package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.request.ApplicationCreateRequest;
import app.intelligent.resume.dto.request.ApplicationStatusRequest;
import app.intelligent.resume.dto.request.MatchRequest;
import app.intelligent.resume.dto.response.ApplicationResponse;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.entity.JobApplication;
import app.intelligent.resume.entity.MatchRecord;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.repository.JobApplicationRepository;
import app.intelligent.resume.repository.JobRepository;
import app.intelligent.resume.repository.MatchRecordRepository;
import app.intelligent.resume.repository.ResumeDetailRepository;
import app.intelligent.resume.repository.ResumeRepository;
import app.intelligent.resume.service.IApplicationService;
import app.intelligent.resume.service.IMatchService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投递服务实现类
 * 对应需求文档4.8投递模块
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
public class ApplicationServiceImpl extends ServiceImpl<JobApplicationRepository, JobApplication> 
        implements IApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeDetailRepository resumeDetailRepository;
    private final MatchRecordRepository matchRecordRepository;
    private final IMatchService matchService;

    public ApplicationServiceImpl(
            JobApplicationRepository applicationRepository,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            ResumeDetailRepository resumeDetailRepository,
            MatchRecordRepository matchRecordRepository,
            @Lazy IMatchService matchService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.resumeDetailRepository = resumeDetailRepository;
        this.matchRecordRepository = matchRecordRepository;
        this.matchService = matchService;
    }

    /**
     * 投递状态常量
     */
    private static final int STATUS_PENDING = 0;      // 待查看
    private static final int STATUS_VIEWED = 1;       // 已查看
    private static final int STATUS_PASSED = 2;       // 通过筛选
    private static final int STATUS_REJECTED = 3;    // 不合适
    private static final int STATUS_OFFERED = 4;     // 已发offer

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobApplication applyJob(Long userId, ApplicationCreateRequest request) {
        log.info("用户 {} 投递岗位 {}, 简历 {}", userId, request.getJobId(), request.getResumeId());

        // 1. 检查是否已投递
        if (hasApplied(userId, request.getJobId())) {
            throw new BusinessException(ResultCode.FAILED, "您已投递过该岗位，请勿重复投递");
        }

        // 2. 检查岗位是否存在且在招聘中
        Job job = jobRepository.selectById(request.getJobId());
        if (job == null) {
            throw new BusinessException(ResultCode.FAILED, "岗位不存在");
        }
        if (job.getStatus() != 1) {
            throw new BusinessException(ResultCode.FAILED, "该岗位已停止招聘");
        }

        // 3. 检查简历是否存在且属于当前用户
        Resume resume = resumeRepository.selectById(request.getResumeId());
        if (resume == null) {
            throw new BusinessException(ResultCode.FAILED, "简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FAILED, "只能使用自己的简历投递");
        }

        // 4. 创建投递记录
        JobApplication application = new JobApplication();
        application.setJobId(request.getJobId());
        application.setResumeId(request.getResumeId());
        application.setUserId(userId);
        application.setCoverLetter(request.getCoverLetter());
        application.setApplicationStatus(STATUS_PENDING);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        applicationRepository.insert(application);

        // 5. 更新岗位申请次数
        job.setApplyCount(job.getApplyCount() == null ? 1 : job.getApplyCount() + 1);
        jobRepository.updateById(job);

        // 6. 自动计算匹配度（如果还没有匹配记录）
        try {
            MatchRecord existingMatch = matchRecordRepository.selectByResumeAndJob(
                    request.getResumeId(), request.getJobId());
            if (existingMatch == null) {
                MatchRequest matchRequest = new MatchRequest();
                matchRequest.setResumeId(request.getResumeId());
                matchRequest.setJobId(request.getJobId());
                matchService.match(matchRequest);
                log.info("投递时自动计算匹配度完成");
            }
        } catch (Exception e) {
            log.warn("投递时自动计算匹配度失败，不影响投递", e);
        }

        log.info("投递成功，投递ID: {}", application.getId());
        return application;
    }

    @Override
    public Page<ApplicationResponse> getMyApplications(Long userId, Integer status, int page, int size) {
        log.info("获取用户 {} 的投递记录，状态: {}, 页码: {}", userId, status, page);

        // 构建查询条件
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getUserId, userId);
        if (status != null) {
            wrapper.eq(JobApplication::getApplicationStatus, status);
        }
        wrapper.orderByDesc(JobApplication::getCreateTime);

        // 分页查询
        Page<JobApplication> applicationPage = new Page<>(page, size);
        applicationRepository.selectPage(applicationPage, wrapper);

        // 转换为响应DTO，并填充岗位信息
        Page<ApplicationResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(applicationPage.getTotal());
        responsePage.setPages(applicationPage.getPages());

        List<ApplicationResponse> responseList = applicationPage.getRecords().stream()
                .map(this::convertToResponseWithJob)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    @Override
    public Page<ApplicationResponse> getJobApplications(Long jobId, Integer status, int page, int size) {
        log.info("获取岗位 {} 的投递记录，状态: {}, 页码: {}", jobId, status, page);

        // 构建查询条件
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getJobId, jobId);
        if (status != null) {
            wrapper.eq(JobApplication::getApplicationStatus, status);
        }
        wrapper.orderByDesc(JobApplication::getCreateTime);

        // 分页查询
        Page<JobApplication> applicationPage = new Page<>(page, size);
        applicationRepository.selectPage(applicationPage, wrapper);

        // 转换为响应DTO，并填充简历信息
        Page<ApplicationResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(applicationPage.getTotal());
        responsePage.setPages(applicationPage.getPages());

        List<ApplicationResponse> responseList = applicationPage.getRecords().stream()
                .map(this::convertToResponseWithResume)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JobApplication updateApplicationStatus(Long applicationId, ApplicationStatusRequest request) {
        log.info("更新投递 {} 状态为 {}", applicationId, request.getStatus());

        JobApplication application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.FAILED, "投递记录不存在");
        }

        // 更新状态和反馈
        application.setApplicationStatus(request.getStatus());
        if (request.getFeedback() != null) {
            application.setHrFeedback(request.getFeedback());
        }
        application.setUpdateTime(LocalDateTime.now());

        applicationRepository.updateById(application);

        log.info("投递状态更新成功");
        return application;
    }

    @Override
    public ApplicationResponse getApplicationDetail(Long applicationId) {
        JobApplication application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.FAILED, "投递记录不存在");
        }

        // 同时填充岗位和简历信息
        ApplicationResponse response = convertToResponseWithJob(application);
        fillResumeInfo(response, application.getResumeId());
        return response;
    }

    @Override
    public boolean hasApplied(Long userId, Long jobId) {
        return applicationRepository.countByUserIdAndJobId(userId, jobId) > 0;
    }

    @Override
    public boolean hasPermission(Long applicationId, Long hrUserId) {
        JobApplication application = applicationRepository.selectById(applicationId);
        if (application == null) {
            return false;
        }

        // 检查岗位是否属于该HR
        Job job = jobRepository.selectById(application.getJobId());
        return job != null && job.getPublisherId().equals(hrUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawApplication(Long applicationId, Long userId) {
        log.info("用户 {} 撤回投递 {}", userId, applicationId);

        JobApplication application = applicationRepository.selectById(applicationId);
        if (application == null) {
            throw new BusinessException(ResultCode.FAILED, "投递记录不存在");
        }

        if (!application.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FAILED, "只能撤回自己的投递");
        }

        // 只有待查看状态可以撤回
        if (application.getApplicationStatus() != STATUS_PENDING) {
            throw new BusinessException(ResultCode.FAILED, "该投递已被处理，无法撤回");
        }

        // 删除投递记录
        applicationRepository.deleteById(applicationId);

        // 更新岗位申请次数
        Job job = jobRepository.selectById(application.getJobId());
        if (job != null && job.getApplyCount() != null && job.getApplyCount() > 0) {
            job.setApplyCount(job.getApplyCount() - 1);
            jobRepository.updateById(job);
        }

        log.info("投递撤回成功");
    }

    // ========== 私有方法 ==========

    /**
     * 转换为响应DTO并填充岗位信息
     */
    private ApplicationResponse convertToResponseWithJob(JobApplication application) {
        ApplicationResponse response = BeanUtil.copyProperties(application, ApplicationResponse.class);

        // 填充岗位信息
        Job job = jobRepository.selectById(application.getJobId());
        if (job != null) {
            response.setJobTitle(job.getJobTitle());
            response.setCompanyName(job.getCompanyName());
            response.setCity(job.getCity());
            response.setSalaryRange(job.getSalaryRange());
            // 填充HR用户ID，用于求职者发起会话
            response.setHrUserId(job.getPublisherId());
        }

        // 填充匹配度
        fillMatchScore(response, application.getResumeId(), application.getJobId());

        return response;
    }

    /**
     * 转换为响应DTO并填充简历信息
     */
    private ApplicationResponse convertToResponseWithResume(JobApplication application) {
        ApplicationResponse response = BeanUtil.copyProperties(application, ApplicationResponse.class);
        
        // 填充岗位信息
        Job job = jobRepository.selectById(application.getJobId());
        if (job != null) {
            response.setJobTitle(job.getJobTitle());
            response.setCompanyName(job.getCompanyName());
        }
        
        fillResumeInfo(response, application.getResumeId());
        
        // 填充匹配度
        fillMatchScore(response, application.getResumeId(), application.getJobId());
        
        return response;
    }

    /**
     * 填充简历信息
     */
    private void fillResumeInfo(ApplicationResponse response, Long resumeId) {
        Resume resume = resumeRepository.selectById(resumeId);
        if (resume != null) {
            response.setResumeTitle(resume.getTitle());

            // 获取简历详情
            ResumeDetail detail = resumeDetailRepository.selectByResumeId(resumeId);
            if (detail != null) {
                response.setSeekerName(detail.getName());
                response.setUserName(detail.getName()); // 设置userName别名
                response.setSeekerPhone(detail.getPhone());
                response.setSeekerEmail(detail.getEmail());
                response.setSkillTags(detail.getSkillTags());
            }
        }
    }

    /**
     * 填充匹配度分数
     */
    private void fillMatchScore(ApplicationResponse response, Long resumeId, Long jobId) {
        if (resumeId == null || jobId == null) {
            return;
        }
        
        MatchRecord matchRecord = matchRecordRepository.selectByResumeAndJob(resumeId, jobId);
        
        // 如果没有匹配记录，尝试自动计算
        if (matchRecord == null) {
            try {
                MatchRequest matchRequest = new MatchRequest();
                matchRequest.setResumeId(resumeId);
                matchRequest.setJobId(jobId);
                matchService.match(matchRequest);
                // 重新查询
                matchRecord = matchRecordRepository.selectByResumeAndJob(resumeId, jobId);
            } catch (Exception e) {
                log.warn("自动计算匹配度失败，resumeId: {}, jobId: {}", resumeId, jobId, e);
            }
        }
        
        if (matchRecord != null && matchRecord.getMatchScore() != null) {
            response.setMatchScore(matchRecord.getMatchScore().intValue());
        }
    }
}
