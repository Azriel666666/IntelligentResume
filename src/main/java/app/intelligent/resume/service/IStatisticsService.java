package app.intelligent.resume.service;

import app.intelligent.resume.dto.response.AdminStatisticsResponse;
import app.intelligent.resume.dto.response.HrStatisticsResponse;
import app.intelligent.resume.dto.response.SeekerStatisticsResponse;

/**
 * 数据统计服务接口
 *
 * @author Intelligent Resume Team
 */
public interface IStatisticsService {

    /**
     * 获取求职者统计数据
     *
     * @param userId 用户ID
     * @return 求职者统计数据
     */
    SeekerStatisticsResponse getSeekerStatistics(Long userId);

    /**
     * 获取HR统计数据
     *
     * @param userId HR用户ID
     * @return HR统计数据
     */
    HrStatisticsResponse getHrStatistics(Long userId);

    /**
     * 获取管理员统计数据
     *
     * @return 管理员统计数据
     */
    AdminStatisticsResponse getAdminStatistics();
}
