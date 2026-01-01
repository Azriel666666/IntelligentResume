package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.JobQueryRequest;
import app.intelligent.resume.dto.response.JobDetailResponse;
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

    // ========== 新增方法 ==========

    /**
     * 条件查询岗位列表（分页）
     * 
     * @param queryRequest 查询条件
     * @return 分页结果
     */
    Page<Job> searchJobs(JobQueryRequest queryRequest);

    /**
     * 获取岗位详情（包含发布者信息）
     * 
     * @param id 岗位ID
     * @return 岗位详情
     */
    JobDetailResponse getJobDetail(Long id);

    /**
     * 更新岗位状态（上架/下架/暂停）
     * 
     * @param id 岗位ID
     * @param status 状态：0-下架 1-招聘中 2-暂停
     * @return 更新后的岗位
     */
    Job updateJobStatus(Long id, Integer status);

    /**
     * 获取当前HR发布的岗位列表
     * 
     * @param hrUserId HR用户ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<Job> getMyJobs(Long hrUserId, Integer status, int page, int size);

    /**
     * 根据求职者简历推荐岗位
     * 
     * @param userId 求职者用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 推荐岗位列表（按匹配度排序）
     */
    Page<JobDetailResponse> recommendJobs(Long userId, int page, int size);

    /**
     * 检查用户是否有权限操作该岗位
     * 
     * @param jobId 岗位ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long jobId, Long userId);

    /**
     * 增加申请次数
     * 
     * @param id 岗位ID
     */
    void incrementApplyCount(Long id);
}
