package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.BatchMatchRequest;
import app.intelligent.resume.dto.request.MatchRequest;
import app.intelligent.resume.dto.response.CandidateRecommendResponse;
import app.intelligent.resume.dto.response.MatchResponse;
import app.intelligent.resume.entity.MatchRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 匹配服务接口
 * 对应需求文档4.7匹配模块
 *
 * @author Intelligent Resume Team
 */
public interface IMatchService extends IService<MatchRecord> {

    /**
     * 4.7.1 简历-岗位匹配
     * 计算简历与岗位的匹配度
     *
     * @param request 匹配请求
     * @return 匹配结果
     */
    MatchResponse match(MatchRequest request);

    /**
     * 4.7.3 获取简历的匹配记录
     *
     * @param resumeId 简历ID
     * @return 匹配记录列表
     */
    List<MatchResponse> getMatchesByResume(Long resumeId);

    /**
     * 4.7.4 获取岗位的匹配记录（候选人列表）
     *
     * @param jobId 岗位ID
     * @param page 页码
     * @param size 每页数量
     * @return 分页匹配记录
     */
    Page<MatchResponse> getMatchesByJob(Long jobId, int page, int size);

    /**
     * 4.7.5 批量匹配
     * 将岗位与多份简历进行批量匹配
     *
     * @param request 批量匹配请求
     * @return 匹配结果列表
     */
    List<MatchResponse> batchMatch(BatchMatchRequest request);

    /**
     * 4.7.6 智能推荐候选人（AI增强）
     * 为岗位智能推荐匹配度高的候选人
     *
     * @param jobId 岗位ID
     * @param page 页码
     * @param size 每页数量
     * @return 推荐候选人列表
     */
    Page<CandidateRecommendResponse> recommendCandidates(Long jobId, int page, int size);

    /**
     * 检查用户是否有权限查看匹配记录
     *
     * @param resumeId 简历ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionForResume(Long resumeId, Long userId);

    /**
     * 检查用户是否有权限查看岗位匹配记录
     *
     * @param jobId 岗位ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasPermissionForJob(Long jobId, Long userId);
}
