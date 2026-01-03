package app.intelligent.resume.service.impl;

import app.intelligent.resume.dto.response.AdminStatisticsResponse;
import app.intelligent.resume.dto.response.HrStatisticsResponse;
import app.intelligent.resume.dto.response.SeekerStatisticsResponse;
import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.entity.Job;
import app.intelligent.resume.entity.JobApplication;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.*;
import app.intelligent.resume.service.IStatisticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据统计服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewInvitationRepository interviewRepository;
    private final AnalysisReportRepository analysisReportRepository;

    @Override
    public SeekerStatisticsResponse getSeekerStatistics(Long userId) {
        // 简历数量
        LambdaQueryWrapper<Resume> resumeWrapper = new LambdaQueryWrapper<>();
        resumeWrapper.eq(Resume::getUserId, userId).eq(Resume::getDeleted, 0);
        long resumeCount = resumeRepository.selectCount(resumeWrapper);

        // 投递次数
        LambdaQueryWrapper<JobApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(JobApplication::getUserId, userId);
        long applicationCount = applicationRepository.selectCount(appWrapper);

        // 被查看次数（简历总浏览量）
        List<Resume> resumes = resumeRepository.selectByUserId(userId);
        int viewedCount = resumes.stream()
                .mapToInt(r -> r.getViewCount() != null ? r.getViewCount() : 0)
                .sum();

        // 面试邀请数
        long interviewCount = countSeekerInterviews(userId);

        // 获取默认简历（优先使用默认简历，否则使用最新简历）
        Resume defaultResume = getDefaultResume(resumes);
        Long defaultResumeId = defaultResume != null ? defaultResume.getId() : null;
        
        // 使用默认简历的分数
        BigDecimal averageScore = BigDecimal.ZERO;
        if (defaultResume != null && defaultResume.getTotalScore() != null) {
            averageScore = defaultResume.getTotalScore();
        }
        
        // 检查默认简历是否有分析报告
        boolean hasAnalysisReport = false;
        if (defaultResumeId != null) {
            AnalysisReport analysis = analysisReportRepository.selectLatestByResumeId(defaultResumeId);
            hasAnalysisReport = analysis != null;
        }

        // 通过筛选数（状态=2）
        LambdaQueryWrapper<JobApplication> passedWrapper = new LambdaQueryWrapper<>();
        passedWrapper.eq(JobApplication::getUserId, userId).eq(JobApplication::getApplicationStatus, 2);
        long passedCount = applicationRepository.selectCount(passedWrapper);

        // 待处理投递数（状态=0或1）
        LambdaQueryWrapper<JobApplication> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(JobApplication::getUserId, userId).in(JobApplication::getApplicationStatus, 0, 1);
        long pendingCount = applicationRepository.selectCount(pendingWrapper);

        // 收到Offer数（状态=4）
        LambdaQueryWrapper<JobApplication> offerWrapper = new LambdaQueryWrapper<>();
        offerWrapper.eq(JobApplication::getUserId, userId).eq(JobApplication::getApplicationStatus, 4);
        long offerCount = applicationRepository.selectCount(offerWrapper);

        return SeekerStatisticsResponse.builder()
                .resumeCount((int) resumeCount)
                .applicationCount((int) applicationCount)
                .viewedCount(viewedCount)
                .interviewCount((int) interviewCount)
                .averageScore(averageScore)
                .passedCount((int) passedCount)
                .pendingCount((int) pendingCount)
                .offerCount((int) offerCount)
                .defaultResumeId(defaultResumeId)
                .hasAnalysisReport(hasAnalysisReport)
                .build();
    }

    /**
     * 获取默认简历（优先使用isDefault=1的，否则使用最新的）
     */
    private Resume getDefaultResume(List<Resume> resumes) {
        if (resumes == null || resumes.isEmpty()) {
            return null;
        }
        
        // 优先查找默认简历
        Resume defaultResume = resumes.stream()
                .filter(r -> r.getIsDefault() != null && r.getIsDefault() == 1)
                .findFirst()
                .orElse(null);
        
        if (defaultResume != null) {
            return defaultResume;
        }
        
        // 没有默认简历，返回最新的简历
        return resumes.stream()
                .max((r1, r2) -> {
                    if (r1.getCreateTime() == null) return -1;
                    if (r2.getCreateTime() == null) return 1;
                    return r1.getCreateTime().compareTo(r2.getCreateTime());
                })
                .orElse(null);
    }

    @Override
    public HrStatisticsResponse getHrStatistics(Long userId) {
        // 发布岗位数
        LambdaQueryWrapper<Job> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(Job::getPublisherId, userId).eq(Job::getDeleted, 0);
        long jobCount = jobRepository.selectCount(jobWrapper);

        // 招聘中岗位数
        LambdaQueryWrapper<Job> activeJobWrapper = new LambdaQueryWrapper<>();
        activeJobWrapper.eq(Job::getPublisherId, userId)
                .eq(Job::getStatus, 1)
                .eq(Job::getDeleted, 0);
        long activeJobCount = jobRepository.selectCount(activeJobWrapper);

        // 获取HR发布的所有岗位ID
        List<Job> jobs = jobRepository.selectByPublisherId(userId);
        List<Long> jobIds = jobs.stream().map(Job::getId).toList();

        // 收到投递数
        long applicationCount = 0;
        long todayApplicationCount = 0;
        long pendingApplicationCount = 0;
        long totalViewCount = 0;

        if (!jobIds.isEmpty()) {
            // 收到投递数
            LambdaQueryWrapper<JobApplication> appWrapper = new LambdaQueryWrapper<>();
            appWrapper.in(JobApplication::getJobId, jobIds);
            applicationCount = applicationRepository.selectCount(appWrapper);

            // 今日新增投递
            LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            LambdaQueryWrapper<JobApplication> todayAppWrapper = new LambdaQueryWrapper<>();
            todayAppWrapper.in(JobApplication::getJobId, jobIds)
                    .ge(JobApplication::getCreateTime, todayStart);
            todayApplicationCount = applicationRepository.selectCount(todayAppWrapper);

            // 待处理投递数（状态=0）
            LambdaQueryWrapper<JobApplication> pendingWrapper = new LambdaQueryWrapper<>();
            pendingWrapper.in(JobApplication::getJobId, jobIds).eq(JobApplication::getApplicationStatus, 0);
            pendingApplicationCount = applicationRepository.selectCount(pendingWrapper);
        }

        // 岗位总浏览量
        totalViewCount = jobs.stream()
                .mapToLong(j -> j.getViewCount() != null ? j.getViewCount() : 0)
                .sum();

        // 已发面试邀请数
        long interviewSentCount = countHrInterviews(userId);

        // 已发Offer数
        long offerSentCount = 0;
        if (!jobIds.isEmpty()) {
            LambdaQueryWrapper<JobApplication> offerWrapper = new LambdaQueryWrapper<>();
            offerWrapper.in(JobApplication::getJobId, jobIds).eq(JobApplication::getApplicationStatus, 4);
            offerSentCount = applicationRepository.selectCount(offerWrapper);
        }

        return HrStatisticsResponse.builder()
                .jobCount((int) jobCount)
                .activeJobCount((int) activeJobCount)
                .applicationCount((int) applicationCount)
                .totalViewCount(totalViewCount)
                .todayApplicationCount((int) todayApplicationCount)
                .pendingApplicationCount((int) pendingApplicationCount)
                .interviewSentCount((int) interviewSentCount)
                .offerSentCount((int) offerSentCount)
                .build();
    }


    @Override
    public AdminStatisticsResponse getAdminStatistics() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");

        // ========== 用户统计 ==========
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        long totalUserCount = userRepository.selectCount(userWrapper);

        // 各类型用户数
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getUserType, 1).eq(User::getDeleted, 0);
        long adminCount = userRepository.selectCount(adminWrapper);

        LambdaQueryWrapper<User> hrWrapper = new LambdaQueryWrapper<>();
        hrWrapper.eq(User::getUserType, 2).eq(User::getDeleted, 0);
        long hrCount = userRepository.selectCount(hrWrapper);

        LambdaQueryWrapper<User> seekerWrapper = new LambdaQueryWrapper<>();
        seekerWrapper.eq(User::getUserType, 3).eq(User::getDeleted, 0);
        long seekerCount = userRepository.selectCount(seekerWrapper);

        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreateTime, todayStart).eq(User::getDeleted, 0);
        long todayNewUserCount = userRepository.selectCount(todayUserWrapper);

        // ========== 简历统计 ==========
        LambdaQueryWrapper<Resume> resumeWrapper = new LambdaQueryWrapper<>();
        resumeWrapper.eq(Resume::getDeleted, 0);
        long totalResumeCount = resumeRepository.selectCount(resumeWrapper);

        // 今日新增简历
        LambdaQueryWrapper<Resume> todayResumeWrapper = new LambdaQueryWrapper<>();
        todayResumeWrapper.ge(Resume::getCreateTime, todayStart).eq(Resume::getDeleted, 0);
        long todayNewResumeCount = resumeRepository.selectCount(todayResumeWrapper);

        // 已解析简历数
        LambdaQueryWrapper<Resume> parsedWrapper = new LambdaQueryWrapper<>();
        parsedWrapper.eq(Resume::getParseStatus, 2).eq(Resume::getDeleted, 0);
        long parsedResumeCount = resumeRepository.selectCount(parsedWrapper);

        // ========== 岗位统计 ==========
        LambdaQueryWrapper<Job> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(Job::getDeleted, 0);
        long totalJobCount = jobRepository.selectCount(jobWrapper);

        // 活跃岗位数
        LambdaQueryWrapper<Job> activeJobWrapper = new LambdaQueryWrapper<>();
        activeJobWrapper.eq(Job::getStatus, 1).eq(Job::getDeleted, 0);
        long activeJobCount = jobRepository.selectCount(activeJobWrapper);

        // 今日新增岗位
        LambdaQueryWrapper<Job> todayJobWrapper = new LambdaQueryWrapper<>();
        todayJobWrapper.ge(Job::getCreateTime, todayStart).eq(Job::getDeleted, 0);
        long todayNewJobCount = jobRepository.selectCount(todayJobWrapper);

        // ========== 投递统计 ==========
        long totalApplicationCount = applicationRepository.selectCount(null);

        // 今日新增投递
        LambdaQueryWrapper<JobApplication> todayAppWrapper = new LambdaQueryWrapper<>();
        todayAppWrapper.ge(JobApplication::getCreateTime, todayStart);
        long todayNewApplicationCount = applicationRepository.selectCount(todayAppWrapper);

        // ========== 趋势数据（近7天） ==========
        List<AdminStatisticsResponse.TrendData> userGrowthTrend = new ArrayList<>();
        List<AdminStatisticsResponse.TrendData> applicationTrend = new ArrayList<>();
        List<AdminStatisticsResponse.TrendData> resumeTrend = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            String dateStr = date.format(formatter);

            // 用户增长
            LambdaQueryWrapper<User> dayUserWrapper = new LambdaQueryWrapper<>();
            dayUserWrapper.between(User::getCreateTime, dayStart, dayEnd).eq(User::getDeleted, 0);
            long dayUserCount = userRepository.selectCount(dayUserWrapper);
            userGrowthTrend.add(AdminStatisticsResponse.TrendData.builder()
                    .date(dateStr).count(dayUserCount).build());

            // 投递趋势
            LambdaQueryWrapper<JobApplication> dayAppWrapper = new LambdaQueryWrapper<>();
            dayAppWrapper.between(JobApplication::getCreateTime, dayStart, dayEnd);
            long dayAppCount = applicationRepository.selectCount(dayAppWrapper);
            applicationTrend.add(AdminStatisticsResponse.TrendData.builder()
                    .date(dateStr).count(dayAppCount).build());

            // 简历趋势
            LambdaQueryWrapper<Resume> dayResumeWrapper = new LambdaQueryWrapper<>();
            dayResumeWrapper.between(Resume::getCreateTime, dayStart, dayEnd).eq(Resume::getDeleted, 0);
            long dayResumeCount = resumeRepository.selectCount(dayResumeWrapper);
            resumeTrend.add(AdminStatisticsResponse.TrendData.builder()
                    .date(dateStr).count(dayResumeCount).build());
        }

        return AdminStatisticsResponse.builder()
                .totalUserCount(totalUserCount)
                .adminCount(adminCount)
                .hrCount(hrCount)
                .seekerCount(seekerCount)
                .todayNewUserCount(todayNewUserCount)
                .totalResumeCount(totalResumeCount)
                .todayNewResumeCount(todayNewResumeCount)
                .parsedResumeCount(parsedResumeCount)
                .totalJobCount(totalJobCount)
                .activeJobCount(activeJobCount)
                .todayNewJobCount(todayNewJobCount)
                .totalApplicationCount(totalApplicationCount)
                .todayNewApplicationCount(todayNewApplicationCount)
                .userGrowthTrend(userGrowthTrend)
                .applicationTrend(applicationTrend)
                .resumeTrend(resumeTrend)
                .build();
    }

    /**
     * 计算简历平均分
     */
    private BigDecimal calculateAverageScore(List<Resume> resumes) {
        if (resumes == null || resumes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<BigDecimal> scores = resumes.stream()
                .filter(r -> r.getTotalScore() != null)
                .map(Resume::getTotalScore)
                .toList();

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = scores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 统计求职者收到的面试邀请数
     */
    private long countSeekerInterviews(Long userId) {
        try {
            return interviewRepository.countBySeekerUserId(userId);
        } catch (Exception e) {
            log.warn("统计面试邀请数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 统计HR发出的面试邀请数
     */
    private long countHrInterviews(Long userId) {
        try {
            return interviewRepository.countByHrUserId(userId);
        } catch (Exception e) {
            log.warn("统计面试邀请数失败: {}", e.getMessage());
            return 0;
        }
    }
}
