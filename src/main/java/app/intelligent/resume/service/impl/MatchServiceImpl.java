package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.request.BatchMatchRequest;
import app.intelligent.resume.dto.request.MatchRequest;
import app.intelligent.resume.dto.response.CandidateRecommendResponse;
import app.intelligent.resume.dto.response.MatchResponse;
import app.intelligent.resume.entity.*;
import app.intelligent.resume.repository.MatchRecordRepository;
import app.intelligent.resume.service.*;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 匹配服务实现类
 * 实现简历与岗位的智能匹配，包含AI增强功能
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl extends ServiceImpl<MatchRecordRepository, MatchRecord> implements IMatchService {

    private final MatchRecordRepository matchRecordRepository;
    private final IResumeService resumeService;
    private final IResumeDetailService resumeDetailService;
    private final IJobService jobService;
    private final IUserService userService;
    private final IQwenService qwenService;
    private final ObjectMapper objectMapper;

    // 学历等级映射
    private static final Map<String, Integer> EDUCATION_LEVEL = Map.of(
            "初中", 1, "高中", 2, "中专", 2, "大专", 3,
            "本科", 4, "硕士", 5, "博士", 6
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchResponse match(MatchRequest request) {
        // 1. 检查是否已有匹配记录
        MatchRecord existingRecord = matchRecordRepository.selectByResumeAndJob(
                request.getResumeId(), request.getJobId());
        if (existingRecord != null) {
            return convertToResponse(existingRecord);
        }

        // 2. 获取简历和岗位信息
        Resume resume = resumeService.getById(request.getResumeId());
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }
        ResumeDetail resumeDetail = resumeDetailService.getByResumeId(request.getResumeId());
        if (resumeDetail == null) {
            // 如果没有简历详情，创建一个空的详情对象
            resumeDetail = new ResumeDetail();
            resumeDetail.setResumeId(request.getResumeId());
        }
        
        Job job = jobService.getById(request.getJobId());
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }

        // 3. 计算匹配度
        MatchRecord matchRecord = calculateMatch(resume, resumeDetail, job, 2);

        // 4. 保存匹配记录
        save(matchRecord);

        return convertToResponse(matchRecord);
    }

    @Override
    public List<MatchResponse> getMatchesByResume(Long resumeId) {
        List<MatchRecord> records = matchRecordRepository.selectByResumeId(resumeId);
        return records.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MatchResponse> getMatchesByJob(Long jobId, int page, int size) {
        Page<MatchRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<MatchRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MatchRecord::getJobId, jobId)
                .orderByDesc(MatchRecord::getMatchScore);
        
        Page<MatchRecord> recordPage = page(pageParam, wrapper);
        
        Page<MatchResponse> responsePage = new Page<>(page, size);
        responsePage.setTotal(recordPage.getTotal());
        responsePage.setRecords(recordPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        
        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MatchResponse> batchMatch(BatchMatchRequest request) {
        Job job = jobService.getById(request.getJobId());
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }

        List<MatchResponse> results = new ArrayList<>();
        
        for (Long resumeId : request.getResumeIds()) {
            try {
                // 检查是否已有匹配记录
                MatchRecord existing = matchRecordRepository.selectByResumeAndJob(resumeId, request.getJobId());
                if (existing != null) {
                    results.add(convertToResponse(existing));
                    continue;
                }

                Resume resume = resumeService.getById(resumeId);
                if (resume == null) continue;
                
                ResumeDetail resumeDetail = resumeDetailService.getByResumeId(resumeId);
                if (resumeDetail == null) {
                    resumeDetail = new ResumeDetail();
                    resumeDetail.setResumeId(resumeId);
                }
                MatchRecord matchRecord = calculateMatch(resume, resumeDetail, job, 1);
                save(matchRecord);
                results.add(convertToResponse(matchRecord));
            } catch (Exception e) {
                log.error("批量匹配失败，resumeId: {}", resumeId, e);
            }
        }

        return results;
    }


    @Override
    public Page<CandidateRecommendResponse> recommendCandidates(Long jobId, int page, int size) {
        Job job = jobService.getById(jobId);
        if (job == null) {
            throw new BusinessException(ResultCode.JOB_NOT_EXIST);
        }

        // 1. 基础筛选：获取符合基本条件的简历
        List<ResumeDetail> candidates = getBasicMatchedCandidates(job);
        
        if (candidates.isEmpty()) {
            return new Page<>(page, size);
        }

        // 2. 计算匹配度并排序
        List<CandidateRecommendResponse> recommendList = new ArrayList<>();
        
        for (ResumeDetail detail : candidates) {
            Resume resume = resumeService.getById(detail.getResumeId());
            if (resume == null || resume.getStatus() != 1) continue;

            // 检查是否已有匹配记录
            MatchRecord existingMatch = matchRecordRepository.selectByResumeAndJob(
                    detail.getResumeId(), jobId);
            
            CandidateRecommendResponse candidate;
            if (existingMatch != null) {
                candidate = buildCandidateFromMatch(detail, resume, existingMatch);
            } else {
                // 计算新的匹配度
                MatchRecord matchRecord = calculateMatch(resume, detail, job, 1);
                save(matchRecord);
                candidate = buildCandidateFromMatch(detail, resume, matchRecord);
            }
            
            recommendList.add(candidate);
        }

        // 3. 按匹配度排序
        recommendList.sort((a, b) -> b.getMatchScore().compareTo(a.getMatchScore()));

        // 4. AI增强：为Top候选人生成推荐理由
        int aiEnhanceCount = Math.min(5, recommendList.size());
        for (int i = 0; i < aiEnhanceCount; i++) {
            CandidateRecommendResponse candidate = recommendList.get(i);
            try {
                enhanceCandidateWithAI(candidate, job);
            } catch (Exception e) {
                log.warn("AI增强推荐失败，candidateId: {}", candidate.getResumeId(), e);
            }
        }

        // 5. 分页返回
        int start = (page - 1) * size;
        int end = Math.min(start + size, recommendList.size());
        
        Page<CandidateRecommendResponse> resultPage = new Page<>(page, size);
        resultPage.setTotal(recommendList.size());
        if (start < recommendList.size()) {
            resultPage.setRecords(recommendList.subList(start, end));
        } else {
            resultPage.setRecords(new ArrayList<>());
        }
        
        return resultPage;
    }

    /**
     * 获取基本条件匹配的候选人
     */
    private List<ResumeDetail> getBasicMatchedCandidates(Job job) {
        LambdaQueryWrapper<ResumeDetail> wrapper = new LambdaQueryWrapper<>();
        
        // 城市匹配（期望城市包含岗位城市，或者不限）
        if (StrUtil.isNotBlank(job.getCity())) {
            wrapper.and(w -> w
                    .like(ResumeDetail::getExpectedCity, job.getCity())
                    .or().isNull(ResumeDetail::getExpectedCity)
                    .or().eq(ResumeDetail::getExpectedCity, "")
                    .or().like(ResumeDetail::getExpectedCity, "不限")
            );
        }

        // 工作年限匹配
        if (job.getWorkYearsMin() != null && job.getWorkYearsMin() > 0) {
            wrapper.ge(ResumeDetail::getWorkYears, job.getWorkYearsMin() - 1); // 允许1年误差
        }

        // 限制数量，避免查询过多
        wrapper.last("LIMIT 100");

        return resumeDetailService.list(wrapper);
    }

    /**
     * 从匹配记录构建候选人响应
     */
    private CandidateRecommendResponse buildCandidateFromMatch(ResumeDetail detail, Resume resume, MatchRecord match) {
        CandidateRecommendResponse response = CandidateRecommendResponse.builder()
                .resumeId(resume.getId())
                .userId(resume.getUserId())
                .name(detail.getName())
                .phone(maskPhone(detail.getPhone()))
                .email(detail.getEmail())
                .highestEducation(detail.getHighestEducation())
                .workYears(detail.getWorkYears())
                .currentCity(detail.getCurrentCity())
                .expectedPosition(detail.getExpectedPosition())
                .expectedSalary(detail.getExpectedSalary())
                .skillTags(detail.getSkillTags())
                .matchScore(match.getMatchScore())
                .skillMatchScore(match.getSkillMatchScore())
                .experienceMatchScore(match.getExperienceMatchScore())
                .educationMatchScore(match.getEducationMatchScore())
                .build();

        // 解析匹配的技能
        if (StrUtil.isNotBlank(match.getMatchedSkills())) {
            try {
                response.setMatchedSkills(objectMapper.readValue(match.getMatchedSkills(), 
                        new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析matchedSkills失败", e);
            }
        }
        if (StrUtil.isNotBlank(match.getMissingSkills())) {
            try {
                response.setMissingSkills(objectMapper.readValue(match.getMissingSkills(), 
                        new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析missingSkills失败", e);
            }
        }

        // 设置推荐等级
        response.setRecommendLevel(calculateRecommendLevel(match.getMatchScore()));

        return response;
    }

    /**
     * AI增强：生成推荐理由
     */
    private void enhanceCandidateWithAI(CandidateRecommendResponse candidate, Job job) {
        if (!qwenService.isAvailable()) {
            candidate.setRecommendReason("基于技能和经验匹配度推荐");
            return;
        }

        String prompt = String.format("""
                你是一位专业的HR招聘专家。请根据以下信息，用一句话说明为什么推荐这位候选人。
                
                【岗位信息】
                职位：%s
                技能要求：%s
                
                【候选人信息】
                学历：%s
                工作年限：%d年
                技能：%s
                匹配度：%.1f%%
                匹配技能：%s
                
                请直接返回推荐理由（一句话，不超过50字），不要其他内容。
                """,
                job.getJobTitle(),
                job.getSkillTags(),
                candidate.getHighestEducation(),
                candidate.getWorkYears() != null ? candidate.getWorkYears() : 0,
                candidate.getSkillTags(),
                candidate.getMatchScore(),
                candidate.getMatchedSkills() != null ? String.join(",", candidate.getMatchedSkills()) : ""
        );

        String reason = qwenService.chat(prompt);
        if (StrUtil.isNotBlank(reason)) {
            candidate.setRecommendReason(reason.trim());
        } else {
            candidate.setRecommendReason("技能匹配度高，经验符合要求");
        }
    }

    /**
     * 计算推荐等级
     */
    private String calculateRecommendLevel(BigDecimal matchScore) {
        if (matchScore == null) return "C";
        double score = matchScore.doubleValue();
        if (score >= 85) return "A";
        if (score >= 70) return "B";
        return "C";
    }

    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (StrUtil.isBlank(phone) || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }


    /**
     * 计算简历与岗位的匹配度（核心算法）
     */
    private MatchRecord calculateMatch(Resume resume, ResumeDetail detail, Job job, int matchType) {
        MatchRecord record = new MatchRecord();
        record.setResumeId(resume.getId());
        record.setJobId(job.getId());
        record.setMatchType(matchType);

        // 1. 技能匹配（权重40%）
        SkillMatchResult skillResult = calculateSkillMatch(detail, job);
        record.setSkillMatchScore(skillResult.score);
        record.setMatchedSkills(toJson(skillResult.matchedSkills));
        record.setMissingSkills(toJson(skillResult.missingSkills));

        // 2. 经验匹配（权重30%）
        BigDecimal experienceScore = calculateExperienceMatch(detail, job);
        record.setExperienceMatchScore(experienceScore);

        // 3. 学历匹配（权重15%）
        BigDecimal educationScore = calculateEducationMatch(detail, job);
        record.setEducationMatchScore(educationScore);

        // 4. 其他匹配（权重15%）- 城市、薪资等
        BigDecimal otherScore = calculateOtherMatch(detail, job);

        // 5. 计算综合得分
        BigDecimal totalScore = skillResult.score.multiply(new BigDecimal("0.40"))
                .add(experienceScore.multiply(new BigDecimal("0.30")))
                .add(educationScore.multiply(new BigDecimal("0.15")))
                .add(otherScore.multiply(new BigDecimal("0.15")))
                .setScale(1, RoundingMode.HALF_UP);
        record.setMatchScore(totalScore);

        // 6. 构建匹配详情
        MatchResponse.MatchDetail matchDetail = MatchResponse.MatchDetail.builder()
                .skillDetail(String.format("匹配技能%d项，缺失%d项", 
                        skillResult.matchedSkills.size(), skillResult.missingSkills.size()))
                .experienceDetail(buildExperienceDetail(detail, job))
                .educationDetail(buildEducationDetail(detail, job))
                .otherDetail(buildOtherDetail(detail, job))
                .build();
        record.setMatchDetail(toJson(matchDetail));

        // 7. AI分析（可选，仅对高匹配度候选人）
        if (totalScore.compareTo(new BigDecimal("70")) >= 0 && qwenService.isAvailable()) {
            try {
                String aiAnalysis = generateAIAnalysis(detail, job, skillResult);
                record.setAiAnalysis(aiAnalysis);
            } catch (Exception e) {
                log.warn("AI分析生成失败", e);
            }
        }

        return record;
    }

    /**
     * 技能匹配结果
     */
    private static class SkillMatchResult {
        BigDecimal score;
        List<String> matchedSkills;
        List<String> missingSkills;
    }

    /**
     * 计算技能匹配度
     */
    private SkillMatchResult calculateSkillMatch(ResumeDetail detail, Job job) {
        SkillMatchResult result = new SkillMatchResult();
        result.matchedSkills = new ArrayList<>();
        result.missingSkills = new ArrayList<>();

        // 获取岗位要求的技能
        Set<String> jobSkills = parseSkills(job.getSkillTags());
        if (jobSkills.isEmpty()) {
            result.score = new BigDecimal("80"); // 无技能要求，给基础分
            return result;
        }

        // 获取简历技能
        Set<String> resumeSkills = parseSkills(detail.getSkillTags());

        // 计算匹配
        for (String jobSkill : jobSkills) {
            boolean matched = resumeSkills.stream()
                    .anyMatch(rs -> isSkillMatch(rs, jobSkill));
            if (matched) {
                result.matchedSkills.add(jobSkill);
            } else {
                result.missingSkills.add(jobSkill);
            }
        }

        // 计算得分
        double matchRate = (double) result.matchedSkills.size() / jobSkills.size();
        result.score = BigDecimal.valueOf(matchRate * 100).setScale(1, RoundingMode.HALF_UP);

        return result;
    }

    /**
     * 判断技能是否匹配（支持模糊匹配）
     */
    private boolean isSkillMatch(String resumeSkill, String jobSkill) {
        String rs = resumeSkill.toLowerCase().trim();
        String js = jobSkill.toLowerCase().trim();
        
        // 完全匹配
        if (rs.equals(js)) return true;
        
        // 包含匹配
        if (rs.contains(js) || js.contains(rs)) return true;
        
        // 常见同义词匹配
        Map<String, List<String>> synonyms = Map.of(
                "java", List.of("jdk", "jvm", "j2ee"),
                "javascript", List.of("js", "es6", "ecmascript"),
                "typescript", List.of("ts"),
                "python", List.of("py"),
                "mysql", List.of("sql", "数据库"),
                "redis", List.of("缓存"),
                "spring", List.of("springboot", "spring boot", "springmvc"),
                "vue", List.of("vue.js", "vuejs"),
                "react", List.of("reactjs", "react.js")
        );
        
        for (Map.Entry<String, List<String>> entry : synonyms.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if ((rs.contains(key) || values.stream().anyMatch(rs::contains)) &&
                (js.contains(key) || values.stream().anyMatch(js::contains))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 解析技能标签
     */
    private Set<String> parseSkills(String skillTags) {
        if (StrUtil.isBlank(skillTags)) {
            return new HashSet<>();
        }
        return Arrays.stream(skillTags.split("[,，、;；/]"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
    }

    /**
     * 计算经验匹配度
     */
    private BigDecimal calculateExperienceMatch(ResumeDetail detail, Job job) {
        Integer resumeYears = detail.getWorkYears();
        Integer jobMinYears = job.getWorkYearsMin();
        Integer jobMaxYears = job.getWorkYearsMax();

        if (jobMinYears == null || jobMinYears == 0) {
            return new BigDecimal("100"); // 无经验要求
        }

        if (resumeYears == null) {
            return new BigDecimal("50"); // 简历未填写经验
        }

        // 完全符合
        if (resumeYears >= jobMinYears && (jobMaxYears == null || resumeYears <= jobMaxYears)) {
            return new BigDecimal("100");
        }

        // 经验不足
        if (resumeYears < jobMinYears) {
            int diff = jobMinYears - resumeYears;
            if (diff == 1) return new BigDecimal("80");
            if (diff == 2) return new BigDecimal("60");
            return new BigDecimal("40");
        }

        // 经验超出（通常不是问题）
        return new BigDecimal("90");
    }

    /**
     * 计算学历匹配度
     */
    private BigDecimal calculateEducationMatch(ResumeDetail detail, Job job) {
        String resumeEdu = detail.getHighestEducation();
        String jobEdu = job.getEducationRequired();

        if (StrUtil.isBlank(jobEdu) || "不限".equals(jobEdu)) {
            return new BigDecimal("100");
        }

        if (StrUtil.isBlank(resumeEdu)) {
            return new BigDecimal("50");
        }

        int resumeLevel = EDUCATION_LEVEL.getOrDefault(resumeEdu, 0);
        int jobLevel = EDUCATION_LEVEL.getOrDefault(jobEdu, 0);

        if (resumeLevel >= jobLevel) {
            return new BigDecimal("100");
        }

        // 学历不足
        int diff = jobLevel - resumeLevel;
        if (diff == 1) return new BigDecimal("70");
        return new BigDecimal("40");
    }

    /**
     * 计算其他匹配度（城市、薪资等）
     */
    private BigDecimal calculateOtherMatch(ResumeDetail detail, Job job) {
        double score = 100;

        // 城市匹配
        if (StrUtil.isNotBlank(job.getCity()) && StrUtil.isNotBlank(detail.getExpectedCity())) {
            if (!detail.getExpectedCity().contains(job.getCity()) && 
                !detail.getExpectedCity().contains("不限")) {
                score -= 30;
            }
        }

        // 薪资匹配（简单判断）
        // 这里可以扩展更复杂的薪资匹配逻辑

        return BigDecimal.valueOf(Math.max(0, score)).setScale(1, RoundingMode.HALF_UP);
    }


    /**
     * 生成AI分析
     */
    private String generateAIAnalysis(ResumeDetail detail, Job job, SkillMatchResult skillResult) {
        String prompt = String.format("""
                你是一位专业的HR招聘专家。请简要分析这位候选人与岗位的匹配情况。
                
                【岗位】%s - %s
                技能要求：%s
                
                【候选人】
                学历：%s，工作年限：%d年
                技能：%s
                匹配技能：%s
                缺失技能：%s
                
                请用2-3句话给出分析和建议（不超过100字），直接返回内容，不要JSON格式。
                """,
                job.getJobTitle(),
                job.getCompanyName(),
                job.getSkillTags(),
                detail.getHighestEducation(),
                detail.getWorkYears() != null ? detail.getWorkYears() : 0,
                detail.getSkillTags(),
                String.join(",", skillResult.matchedSkills),
                String.join(",", skillResult.missingSkills)
        );

        return qwenService.chat(prompt);
    }

    /**
     * 构建经验匹配详情
     */
    private String buildExperienceDetail(ResumeDetail detail, Job job) {
        Integer resumeYears = detail.getWorkYears();
        Integer jobMinYears = job.getWorkYearsMin();
        
        if (jobMinYears == null || jobMinYears == 0) {
            return "岗位无经验要求";
        }
        if (resumeYears == null) {
            return "简历未填写工作年限";
        }
        if (resumeYears >= jobMinYears) {
            return String.format("工作经验%d年，符合要求（要求%d年以上）", resumeYears, jobMinYears);
        }
        return String.format("工作经验%d年，低于要求（要求%d年以上）", resumeYears, jobMinYears);
    }

    /**
     * 构建学历匹配详情
     */
    private String buildEducationDetail(ResumeDetail detail, Job job) {
        String resumeEdu = detail.getHighestEducation();
        String jobEdu = job.getEducationRequired();
        
        if (StrUtil.isBlank(jobEdu) || "不限".equals(jobEdu)) {
            return "岗位无学历要求";
        }
        if (StrUtil.isBlank(resumeEdu)) {
            return "简历未填写学历";
        }
        
        int resumeLevel = EDUCATION_LEVEL.getOrDefault(resumeEdu, 0);
        int jobLevel = EDUCATION_LEVEL.getOrDefault(jobEdu, 0);
        
        if (resumeLevel >= jobLevel) {
            return String.format("学历%s，符合要求（要求%s）", resumeEdu, jobEdu);
        }
        return String.format("学历%s，低于要求（要求%s）", resumeEdu, jobEdu);
    }

    /**
     * 构建其他匹配详情
     */
    private String buildOtherDetail(ResumeDetail detail, Job job) {
        List<String> details = new ArrayList<>();
        
        if (StrUtil.isNotBlank(job.getCity()) && StrUtil.isNotBlank(detail.getExpectedCity())) {
            if (detail.getExpectedCity().contains(job.getCity()) || 
                detail.getExpectedCity().contains("不限")) {
                details.add("期望城市匹配");
            } else {
                details.add("期望城市不匹配");
            }
        }
        
        return details.isEmpty() ? "其他条件匹配" : String.join("，", details);
    }

    /**
     * 转换为响应对象
     */
    private MatchResponse convertToResponse(MatchRecord record) {
        MatchResponse response = MatchResponse.builder()
                .id(record.getId())
                .resumeId(record.getResumeId())
                .jobId(record.getJobId())
                .matchScore(record.getMatchScore())
                .skillMatchScore(record.getSkillMatchScore())
                .experienceMatchScore(record.getExperienceMatchScore())
                .educationMatchScore(record.getEducationMatchScore())
                .aiAnalysis(record.getAiAnalysis())
                .matchType(record.getMatchType())
                .createTime(record.getCreateTime())
                .build();

        // 解析JSON字段
        if (StrUtil.isNotBlank(record.getMatchedSkills())) {
            try {
                response.setMatchedSkills(objectMapper.readValue(record.getMatchedSkills(), 
                        new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析matchedSkills失败", e);
            }
        }
        if (StrUtil.isNotBlank(record.getMissingSkills())) {
            try {
                response.setMissingSkills(objectMapper.readValue(record.getMissingSkills(), 
                        new TypeReference<List<String>>() {}));
            } catch (JsonProcessingException e) {
                log.warn("解析missingSkills失败", e);
            }
        }
        if (StrUtil.isNotBlank(record.getMatchDetail())) {
            try {
                response.setMatchDetail(objectMapper.readValue(record.getMatchDetail(), 
                        MatchResponse.MatchDetail.class));
            } catch (JsonProcessingException e) {
                log.warn("解析matchDetail失败", e);
            }
        }

        // 获取关联信息
        Resume resume = resumeService.getById(record.getResumeId());
        if (resume != null) {
            response.setResumeTitle(resume.getTitle());
            ResumeDetail detail = resumeDetailService.getByResumeId(resume.getId());
            if (detail != null) {
                response.setCandidateName(detail.getName());
            }
        }
        
        Job job = jobService.getById(record.getJobId());
        if (job != null) {
            response.setJobTitle(job.getJobTitle());
            response.setCompanyName(job.getCompanyName());
        }

        return response;
    }

    /**
     * 对象转JSON
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON序列化失败", e);
            return null;
        }
    }

    @Override
    public boolean hasPermissionForResume(Long resumeId, Long userId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) return false;
        return resume.getUserId().equals(userId);
    }

    @Override
    public boolean hasPermissionForJob(Long jobId, Long userId) {
        Job job = jobService.getById(jobId);
        if (job == null) return false;
        
        // 岗位发布者
        if (job.getPublisherId().equals(userId)) return true;
        
        // 管理员
        User user = userService.getById(userId);
        return user != null && user.getUserType() != null && user.getUserType() == 1;
    }
}
