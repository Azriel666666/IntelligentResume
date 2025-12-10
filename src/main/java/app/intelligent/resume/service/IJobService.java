package app.intelligent.resume.service;

import app.intelligent.resume.entity.Job;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 岗位Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IJobService extends IService<Job> {

    /**
     * 根据发布者ID查询岗位列表
     */
    List<Job> listByPublisherId(Long publisherId);

    /**
     * 查询招聘中的岗位
     */
    List<Job> listRecruitingJobs();

    /**
     * 查询所有岗位列表
     */
    List<Job> listAllJobs();

    /**
     * 分页查询岗位
     */
    Page<Job> pageJobs(int page, int size);

    /**
     * 创建岗位
     */
    Job createJob(Job job);

    /**
     * 更新岗位
     */
    Job updateJob(Long id, Job job);

    /**
     * 删除岗位
     */
    boolean deleteJob(Long id);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);
}
