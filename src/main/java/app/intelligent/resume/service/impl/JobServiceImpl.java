package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.repository.JobRepository;
import app.intelligent.resume.service.IJobService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 岗位Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class JobServiceImpl extends ServiceImpl<JobRepository, Job> implements IJobService {

    private final JobRepository jobRepository;

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

        save(job);
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
        if (job.getJobDescription() != null) {
            existingJob.setJobDescription(job.getJobDescription());
        }
        if (job.getJobRequirements() != null) {
            existingJob.setJobRequirements(job.getJobRequirements());
        }
        if (job.getSkillsRequired() != null) {
            existingJob.setSkillsRequired(job.getSkillsRequired());
        }
        if (job.getStatus() != null) {
            existingJob.setStatus(job.getStatus());
        }

        updateById(existingJob);
        return existingJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long id) {
        Job job = getById(id);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }
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
}
