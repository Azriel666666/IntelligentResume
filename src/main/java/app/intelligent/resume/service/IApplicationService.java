package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.ApplicationCreateRequest;
import app.intelligent.resume.dto.request.ApplicationStatusRequest;
import app.intelligent.resume.dto.response.ApplicationResponse;
import app.intelligent.resume.entity.JobApplication;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 投递服务接口
 * 对应需求文档4.8投递模块
 *
 * @author Intelligent Resume Team
 */
public interface IApplicationService extends IService<JobApplication> {

    /**
     * 4.8.1 投递简历
     * 
     * @param userId 当前用户ID
     * @param request 投递请求
     * @return 投递记录
     */
    JobApplication applyJob(Long userId, ApplicationCreateRequest request);

    /**
     * 4.8.2 获取我的投递记录
     * 
     * @param userId 当前用户ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 投递记录列表（带岗位信息）
     */
    Page<ApplicationResponse> getMyApplications(Long userId, Integer status, int page, int size);

    /**
     * 4.8.3 获取岗位收到的投递
     * 
     * @param jobId 岗位ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 投递记录列表（带简历信息）
     */
    Page<ApplicationResponse> getJobApplications(Long jobId, Integer status, int page, int size);

    /**
     * 4.8.4 处理投递（更新状态）
     * 
     * @param applicationId 投递ID
     * @param request 状态更新请求
     * @return 更新后的投递记录
     */
    JobApplication updateApplicationStatus(Long applicationId, ApplicationStatusRequest request);

    /**
     * 获取投递详情
     * 
     * @param applicationId 投递ID
     * @return 投递详情
     */
    ApplicationResponse getApplicationDetail(Long applicationId);

    /**
     * 检查是否已投递
     * 
     * @param userId 用户ID
     * @param jobId 岗位ID
     * @return 是否已投递
     */
    boolean hasApplied(Long userId, Long jobId);

    /**
     * 检查HR是否有权限操作该投递
     * 
     * @param applicationId 投递ID
     * @param hrUserId HR用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long applicationId, Long hrUserId);

    /**
     * 撤回投递
     * 
     * @param applicationId 投递ID
     * @param userId 用户ID
     */
    void withdrawApplication(Long applicationId, Long userId);
}
