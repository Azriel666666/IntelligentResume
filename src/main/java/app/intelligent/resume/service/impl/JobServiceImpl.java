package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.request.JobQueryRequest;
import app.intelligent.resume.dto.response.JobDetailResponse;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.JobRepository;
import app.intelligent.resume.service.IJobService;
import app.intelligent.resume.service.IResumeDetailService;
import app.intelligent.resume.service.IResumeService;
import app.intelligent.resume.service.IUserService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位Service实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<JobRepository, Job> implements IJobService {

    private final JobRepository jobRepository;
    private final IUserService userService;
    private final IResumeService resumeService;
    private final IResumeDetailService resumeDetailService;

    @Override
    public List<Job> listByPublisherId(Long publisherId) {
        return jobRepository.selectByPublisherId(publisherId);
    }

    @Override
    public List<Job> listRecruitingJobs() {
        return jobRepository.selectRecruitingJobs();
    }

    @Override
    public List<Job> listAllJobs() {
        return list();
    }

    @Override
    public Page<Job> pageJobs(int page, int size) {
        return page(new Page<>(page, size));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Job createJob(Job job) {
        // 获取当前用户信息
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            job.setPublisherId(currentUser.getId());
            // 如果没有设置公司名称，使用HR的公司名称
            if (StrUtil.isBlank(job.getCompanyName()) && StrUtil.isNotBlank(currentUser.getCompanyName())) {
                job.setCompanyName(currentUser.getCompanyName());
            }
        }

        // 设置默认值
        if (job.getStatus() == null) {
            job.setStatus(1); // 招聘中
        }
        if (job.getViewCount() == null) {
            job.setViewCount(0);
        }
        if (job.getApplyCount() == null) {
            job.setApplyCount(0);
        }
        if (job.getIsTop() == null) {
            job.setIsTop(0);
        }
        if (job.getRecruiterCount() == null) {
            job.setRecruiterCount(1);
        }

        // 自动生成薪资范围描述
        if (StrUtil.isBlank(job.getSalaryRange()) && job.getSalaryMin() != null && job.getSalaryMax() != null) {
            job.setSalaryRange(job.getSalaryMin().intValue() + "K-" + job.getSalaryMax().intValue() + "K");
        }

        save(job);
        log.info("创建岗位成功，jobId: {}, jobTitle: {}", job.getId(), job.getJobTitle());
        return job;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Job updateJob(Long id, Job job) {
        Job existingJob = getById(id);
        if (existingJob == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }

        // 更新字段
        if (job.getJobTitle() != null) {
            existingJob.setJobTitle(job.getJobTitle());
        }
        if (job.getCompanyName() != null) {
            existingJob.setCompanyName(job.getCompanyName());
        }
        if (job.getSalaryMin() != null) {
            existingJob.setSalaryMin(job.getSalaryMin());
        }
        if (job.getSalaryMax() != null) {
            existingJob.setSalaryMax(job.getSalaryMax());
        }
        if (job.getSalaryRange() != null) {
            existingJob.setSalaryRange(job.getSalaryRange());
        }
        if (job.getCity() != null) {
            existingJob.setCity(job.getCity());
        }
        if (job.getWorkAddress() != null) {
            existingJob.setWorkAddress(job.getWorkAddress());
        }
        if (job.getWorkYearsMin() != null) {
            existingJob.setWorkYearsMin(job.getWorkYearsMin());
        }
        if (job.getWorkYearsMax() != null) {
            existingJob.setWorkYearsMax(job.getWorkYearsMax());
        }
        if (job.getEducationRequired() != null) {
            existingJob.setEducationRequired(job.getEducationRequired());
        }
        if (job.getJobType() != null) {
            existingJob.setJobType(job.getJobType());
        }
        if (job.getDepartment() != null) {
            existingJob.setDepartment(job.getDepartment());
        }
        if (job.getJobDescription() != null) {
            existingJob.setJobDescription(job.getJobDescription());
        }
        if (job.getJobRequirements() != null) {
            existingJob.setJobRequirements(job.getJobRequirements());
        }
        if (job.getSkillsRequired() != null) {
            existingJob.setSkillsRequired(job.getSkillsRequired());
        }
        if (job.getSkillTags() != null) {
            existingJob.setSkillTags(job.getSkillTags());
        }
        if (job.getWelfareTags() != null) {
            existingJob.setWelfareTags(job.getWelfareTags());
        }
        if (job.getRecruiterCount() != null) {
            existingJob.setRecruiterCount(job.getRecruiterCount());
        }
        if (job.getExpireTime() != null) {
            existingJob.setExpireTime(job.getExpireTime());
        }
        if (job.getStatus() != null) {
            existingJob.setStatus(job.getStatus());
        }

        // 更新薪资范围描述
        if (existingJob.getSalaryMin() != null && existingJob.getSalaryMax() != null) {
            existingJob.setSalaryRange(existingJob.getSalaryMin().intValue() + "K-" + existingJob.getSalaryMax().intValue() + "K");
        }

        updateById(existingJob);
        log.info("更新岗位成功，jobId: {}", id);
        return existingJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long id) {
        Job job = getById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }
        log.info("删除岗位，jobId: {}, jobTitle: {}", id, job.getJobTitle());
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        Job job = getById(id);
        if (job != null) {
            job.setViewCount(job.getViewCount() + 1);
            updateById(job);
        }
    }

    @Override
    public Page<Job> searchJobs(JobQueryRequest request) {
        Page<Job> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（职位名称、公司名称、岗位描述）
        if (StrUtil.isNotBlank(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(Job::getJobTitle, request.getKeyword())
                    .or().like(Job::getCompanyName, request.getKeyword())
                    .or().like(Job::getJobDescription, request.getKeyword())
            );
        }
        
        // 城市筛选
        if (StrUtil.isNotBlank(request.getCity())) {
            wrapper.eq(Job::getCity, request.getCity());
        }
        
        // 薪资范围筛选
        if (request.getSalaryMin() != null) {
            wrapper.ge(Job::getSalaryMax, request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            wrapper.le(Job::getSalaryMin, request.getSalaryMax());
        }
        
        // 工作年限筛选
        if (request.getWorkYearsMin() != null) {
            wrapper.le(Job::getWorkYearsMin, request.getWorkYearsMin());
        }
        if (request.getWorkYearsMax() != null) {
            wrapper.ge(Job::getWorkYearsMax, request.getWorkYearsMax());
        }
        
        // 学历要求筛选
        if (StrUtil.isNotBlank(request.getEducationRequired())) {
            wrapper.eq(Job::getEducationRequired, request.getEducationRequired());
        }
        
        // 工作类型筛选
        if (StrUtil.isNotBlank(request.getJobType())) {
            wrapper.eq(Job::getJobType, request.getJobType());
        }
        
        // 技能标签筛选
        if (StrUtil.isNotBlank(request.getSkillTags())) {
            String[] skills = request.getSkillTags().split(",");
            for (String skill : skills) {
                if (StrUtil.isNotBlank(skill.trim())) {
                    wrapper.like(Job::getSkillTags, skill.trim());
                }
            }
        }
        
        // 状态筛选（默认只查询招聘中的）
        if (request.getStatus() != null) {
            wrapper.eq(Job::getStatus, request.getStatus());
        } else {
            wrapper.eq(Job::getStatus, 1); // 默认只显示招聘中
        }
        
        // 排序
        if ("salary".equals(request.getSortBy())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(Job::getSalaryMin);
            } else {
                wrapper.orderByDesc(Job::getSalaryMax);
            }
        } else if ("viewCount".equals(request.getSortBy())) {
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(Job::getViewCount);
            } else {
                wrapper.orderByDesc(Job::getViewCount);
            }
        } else {
            // 默认按创建时间排序，置顶优先
            wrapper.orderByDesc(Job::getIsTop);
            if ("asc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByAsc(Job::getCreateTime);
            } else {
                wrapper.orderByDesc(Job::getCreateTime);
            }
        }
        
        return page(page, wrapper);
    }

    @Override
    public JobDetailResponse getJobDetail(Long id) {
        Job job = getById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }
        
        JobDetailResponse response = BeanUtil.copyProperties(job, JobDetailResponse.class);
        
        // 获取发布者信息
        if (job.getPublisherId() != null) {
            User publisher = userService.getById(job.getPublisherId());
            if (publisher != null) {
                response.setPublisherName(publisher.getNickname() != null ? publisher.getNickname() : publisher.getUsername());
            }
        }
        
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Job updateJobStatus(Long id, Integer status) {
        Job job = getById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }
        
        job.setStatus(status);
        updateById(job);
        
        String statusDesc = switch (status) {
            case 0 -> "下架";
            case 1 -> "上架";
            case 2 -> "暂停";
            default -> "未知";
        };
        log.info("更新岗位状态，jobId: {}, status: {}", id, statusDesc);
        
        return job;
    }

    @Override
    public Page<Job> getMyJobs(Long hrUserId, Integer status, int page, int size) {
        Page<Job> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getPublisherId, hrUserId);
        
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }
        
        wrapper.orderByDesc(Job::getCreateTime);
        
        return page(pageParam, wrapper);
    }

    @Override
    public Page<JobDetailResponse> recommendJobs(Long userId, int page, int size) {
        // 获取用户的默认简历
        Resume defaultResume = resumeService.getDefaultByUserId(userId);
        ResumeDetail resumeDetail = null;
        Set<String> userSkills = new HashSet<>();
        String expectedCity = null;
        String expectedPosition = null;
        Integer workYears = null;
        
        if (defaultResume != null) {
            resumeDetail = resumeDetailService.getByResumeId(defaultResume.getId());
            if (resumeDetail != null) {
                // 提取用户技能
                if (StrUtil.isNotBlank(resumeDetail.getSkillTags())) {
                    userSkills.addAll(Arrays.asList(resumeDetail.getSkillTags().split("[,，、;；]")));
                }
                expectedCity = resumeDetail.getExpectedCity();
                expectedPosition = resumeDetail.getExpectedPosition();
                workYears = resumeDetail.getWorkYears();
            }
        }
        
        // 查询招聘中的岗位
        Page<Job> jobPage = new Page<>(page, size);
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, 1); // 只查询招聘中的
        
        // 如果有期望城市，优先匹配
        if (StrUtil.isNotBlank(expectedCity)) {
            wrapper.eq(Job::getCity, expectedCity);
        }
        
        // 如果有期望职位，模糊匹配
        if (StrUtil.isNotBlank(expectedPosition)) {
            wrapper.like(Job::getJobTitle, expectedPosition);
        }
        
        wrapper.orderByDesc(Job::getIsTop).orderByDesc(Job::getCreateTime);
        
        Page<Job> result = page(jobPage, wrapper);
        
        // 如果没有匹配结果，放宽条件重新查询
        if (result.getRecords().isEmpty()) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Job::getStatus, 1);
            wrapper.orderByDesc(Job::getIsTop).orderByDesc(Job::getViewCount);
            result = page(new Page<>(page, size), wrapper);
        }
        
        // 计算匹配度并转换为响应对象
        final Set<String> finalUserSkills = userSkills;
        final Integer finalWorkYears = workYears;
        
        List<JobDetailResponse> responseList = result.getRecords().stream()
                .map(job -> {
                    JobDetailResponse response = BeanUtil.copyProperties(job, JobDetailResponse.class);
                    // 计算简单匹配度
                    BigDecimal matchScore = calculateSimpleMatchScore(job, finalUserSkills, finalWorkYears);
                    response.setMatchScore(matchScore);
                    return response;
                })
                .sorted((a, b) -> b.getMatchScore().compareTo(a.getMatchScore())) // 按匹配度降序
                .collect(Collectors.toList());
        
        Page<JobDetailResponse> responsePage = new Page<>(page, size);
        responsePage.setRecords(responseList);
        responsePage.setTotal(result.getTotal());
        
        return responsePage;
    }

    /**
     * 计算简单匹配度
     */
    private BigDecimal calculateSimpleMatchScore(Job job, Set<String> userSkills, Integer workYears) {
        double score = 60.0; // 基础分
        
        // 技能匹配（最高+30分）
        if (!userSkills.isEmpty() && StrUtil.isNotBlank(job.getSkillTags())) {
            Set<String> jobSkills = new HashSet<>(Arrays.asList(job.getSkillTags().split("[,，、;；]")));
            long matchedCount = userSkills.stream()
                    .filter(skill -> jobSkills.stream()
                            .anyMatch(js -> js.toLowerCase().contains(skill.toLowerCase().trim()) 
                                    || skill.toLowerCase().trim().contains(js.toLowerCase())))
                    .count();
            if (!jobSkills.isEmpty()) {
                score += (matchedCount * 30.0 / jobSkills.size());
            }
        }
        
        // 工作年限匹配（最高+10分）
        if (workYears != null && job.getWorkYearsMin() != null) {
            if (workYears >= job.getWorkYearsMin()) {
                score += 10;
            } else if (workYears >= job.getWorkYearsMin() - 1) {
                score += 5; // 差1年也给部分分
            }
        }
        
        return BigDecimal.valueOf(Math.min(100, score)).setScale(1, RoundingMode.HALF_UP);
    }

    @Override
    public boolean hasPermission(Long jobId, Long userId) {
        Job job = getById(jobId);
        if (job == null) {
            return false;
        }
        
        // 检查是否是岗位发布者
        if (job.getPublisherId() != null && job.getPublisherId().equals(userId)) {
            return true;
        }
        
        // 检查是否是管理员
        User user = userService.getById(userId);
        if (user != null && user.getUserType() != null && user.getUserType() == 1) {
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementApplyCount(Long id) {
        Job job = getById(id);
        if (job != null) {
            job.setApplyCount(job.getApplyCount() + 1);
            updateById(job);
        }
    }
}
