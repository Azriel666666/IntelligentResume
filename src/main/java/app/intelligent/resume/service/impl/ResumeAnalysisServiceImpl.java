package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.service.*;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisServiceImpl implements IResumeAnalysisService {

    private final IResumeService resumeService;
    private final IResumeDetailService resumeDetailService;
    private final IAnalysisReportService analysisReportService;
    private final IQwenService qwenService;  // 使用阿里云通义千问

    private static final Set<String> HOT_SKILLS = Set.of(
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#",
            "Spring Boot", "Spring Cloud", "MyBatis", "Vue", "React", "Angular",
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch",
            "Docker", "Kubernetes", "Jenkins", "Git", "Linux", "Nginx",
            "AWS", "阿里云", "腾讯云", "微服务", "分布式", "高并发"
    );

    private static final Map<String, List<String>> SKILL_CATEGORIES = Map.of(
            "编程语言", List.of("Java", "Python", "JavaScript", "TypeScript", "Go", "C++", "C#"),
            "后端框架", List.of("Spring Boot", "Spring Cloud", "MyBatis", "Django", "Flask"),
            "前端框架", List.of("Vue", "React", "Angular", "jQuery"),
            "数据库", List.of("MySQL", "PostgreSQL", "MongoDB", "Redis", "Elasticsearch"),
            "云原生", List.of("Docker", "Kubernetes", "Jenkins", "微服务"),
            "云平台", List.of("AWS", "阿里云", "腾讯云", "华为云")
    );

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisReport analyzeResume(Long resumeId) {
        Resume resume = resumeService.getById(resumeId);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }
        ResumeDetail resumeDetail = resumeDetailService.getByResumeId(resumeId);
        if (resumeDetail == null) {
            throw new BusinessException(ResultCode.RESUME_DETAIL_NOT_EXIST);
        }
        return analyzeResumeDetail(resumeId, resumeDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisReport analyzeResumeDetail(Long resumeId, ResumeDetail resumeDetail) {
        log.info("开始分析简历，resumeId: {}", resumeId);

        AnalysisReport report = new AnalysisReport();
        report.setResumeId(resumeId);

        int completenessScore = calculateCompletenessScore(resumeDetail);
        report.setCompletenessScore(BigDecimal.valueOf(completenessScore));
        report.setCompletenessAnalysis(analyzeCompleteness(resumeDetail));

        int formatScore = calculateFormatScore(resumeDetail);
        report.setFormatScore(BigDecimal.valueOf(formatScore));
        report.setSkillAnalysis(analyzeSkills(resumeDetail));

        String resumeText = convertToText(resumeDetail);
        int contentQualityScore = 70;
        String competitiveness = "一般";

        if (qwenService.isAvailable()) {
            try {
                log.info("使用阿里云通义千问进行简历分析，resumeId: {}", resumeId);
                
                String qualityResult = qwenService.analyzeResumeQuality(resumeText);
                if (StrUtil.isNotBlank(qualityResult)) {
                    JSONObject qualityJson = parseJsonSafely(qualityResult);
                    if (qualityJson != null) {
                        contentQualityScore = calculateAverageScore(qualityJson);
                    }
                }

                String suggestionsResult = qwenService.generateSuggestions(resumeText);
                if (StrUtil.isNotBlank(suggestionsResult)) {
                    JSONObject suggestionsJson = parseJsonSafely(suggestionsResult);
                    if (suggestionsJson != null) {
                        report.setSuggestions(suggestionsJson.getStr("suggestions", "[]"));
                        report.setAiSuggestions(suggestionsJson.getStr("prioritySuggestion"));
                    }
                }

                report.setCareerAdvice(qwenService.generateCareerAdvice(resumeText));
                report.setKeywords(qwenService.extractKeywords(resumeText));

                String strengthsResult = qwenService.analyzeStrengthsAndWeaknesses(resumeText);
                if (StrUtil.isNotBlank(strengthsResult)) {
                    JSONObject strengthsJson = parseJsonSafely(strengthsResult);
                    if (strengthsJson != null) {
                        report.setStrengthPoints(strengthsJson.getStr("strengths", "[]"));
                        report.setWeakPoints(strengthsJson.getStr("weaknesses", "[]"));
                        competitiveness = strengthsJson.getStr("competitiveness", "一般");
                    }
                }
                log.info("AI分析完成，resumeId: {}", resumeId);
            } catch (Exception e) {
                log.error("AI分析异常，使用规则引擎结果", e);
                generateDefaultSuggestions(report, resumeDetail);
            }
        } else {
            log.info("通义千问服务不可用，使用规则引擎分析");
            generateDefaultSuggestions(report, resumeDetail);
            contentQualityScore = calculateContentQualityByRules(resumeDetail);
            report.setKeywords(extractKeywordsByRules(resumeDetail));
            competitiveness = calculateCompetitivenessByRules(completenessScore, contentQualityScore, formatScore);
        }

        report.setContentQualityScore(BigDecimal.valueOf(contentQualityScore));
        report.setCompetitiveness(competitiveness);

        double totalScore = completenessScore * 0.4 + contentQualityScore * 0.35 + formatScore * 0.25;
        report.setTotalScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));

        analysisReportService.createReport(report);

        Resume resume = resumeService.getById(resumeId);
        if (resume != null) {
            resume.setTotalScore(report.getTotalScore());
            resume.setCompletenessScore(report.getCompletenessScore());
            resume.setQualityScore(report.getContentQualityScore());
            resume.setFormatScore(report.getFormatScore());
            resumeService.updateById(resume);
        }

        log.info("简历分析完成，resumeId: {}, 总分: {}", resumeId, totalScore);
        return report;
    }

    @Override
    public int calculateCompletenessScore(ResumeDetail detail) {
        int score = 0;
        int totalWeight = 100;

        if (StrUtil.isNotBlank(detail.getName())) score += 5;
        if (StrUtil.isNotBlank(detail.getPhone())) score += 5;
        if (StrUtil.isNotBlank(detail.getEmail())) score += 4;
        if (detail.getGender() != null) score += 2;
        if (detail.getBirthDate() != null || detail.getAge() != null) score += 2;
        if (StrUtil.isNotBlank(detail.getCurrentCity())) score += 2;

        if (StrUtil.isNotBlank(detail.getExpectedPosition())) score += 5;
        if (StrUtil.isNotBlank(detail.getExpectedSalary())) score += 4;
        if (StrUtil.isNotBlank(detail.getExpectedCity())) score += 3;
        if (StrUtil.isNotBlank(detail.getJobStatus())) score += 3;

        if (StrUtil.isNotBlank(detail.getEducationJson()) && !"[]".equals(detail.getEducationJson())) {
            score += 10;
            if (StrUtil.isNotBlank(detail.getHighestEducation())) score += 5;
        }

        if (StrUtil.isNotBlank(detail.getWorkExperienceJson()) && !"[]".equals(detail.getWorkExperienceJson())) {
            score += 15;
            if (detail.getWorkYears() != null && detail.getWorkYears() > 0) score += 5;
        }

        if (StrUtil.isNotBlank(detail.getProjectExperienceJson()) && !"[]".equals(detail.getProjectExperienceJson())) {
            score += 15;
        }

        if (StrUtil.isNotBlank(detail.getSkillsJson()) && !"[]".equals(detail.getSkillsJson())) score += 7;
        if (StrUtil.isNotBlank(detail.getSkillTags())) score += 3;

        if (StrUtil.isNotBlank(detail.getSelfEvaluation())) score += 5;

        return Math.min(100, score);
    }

    @Override
    public String analyzeCompleteness(ResumeDetail detail) {
        JSONObject analysis = new JSONObject();
        analysis.set("hasBasicInfo", StrUtil.isNotBlank(detail.getName()) && StrUtil.isNotBlank(detail.getPhone()));
        analysis.set("hasEducation", StrUtil.isNotBlank(detail.getEducationJson()) && !"[]".equals(detail.getEducationJson()));
        analysis.set("hasWorkExperience", StrUtil.isNotBlank(detail.getWorkExperienceJson()) && !"[]".equals(detail.getWorkExperienceJson()));
        analysis.set("hasProjectExperience", StrUtil.isNotBlank(detail.getProjectExperienceJson()) && !"[]".equals(detail.getProjectExperienceJson()));
        analysis.set("hasSkills", StrUtil.isNotBlank(detail.getSkillsJson()) || StrUtil.isNotBlank(detail.getSkillTags()));
        analysis.set("hasCertificates", StrUtil.isNotBlank(detail.getCertificatesJson()) && !"[]".equals(detail.getCertificatesJson()));
        analysis.set("hasSelfEvaluation", StrUtil.isNotBlank(detail.getSelfEvaluation()));

        List<String> missingItems = new ArrayList<>();
        if (!analysis.getBool("hasBasicInfo")) missingItems.add("基本信息");
        if (!analysis.getBool("hasEducation")) missingItems.add("教育经历");
        if (!analysis.getBool("hasWorkExperience")) missingItems.add("工作经历");
        if (!analysis.getBool("hasProjectExperience")) missingItems.add("项目经验");
        if (!analysis.getBool("hasSkills")) missingItems.add("技能特长");
        if (!analysis.getBool("hasCertificates")) missingItems.add("证书奖项");
        if (!analysis.getBool("hasSelfEvaluation")) missingItems.add("自我评价");
        analysis.set("missingItems", missingItems);

        return analysis.toString();
    }

    @Override
    public String analyzeSkills(ResumeDetail detail) {
        JSONObject analysis = new JSONObject();
        Set<String> skills = new HashSet<>();

        if (StrUtil.isNotBlank(detail.getSkillTags())) {
            String[] tags = detail.getSkillTags().split("[,，、;；]");
            for (String tag : tags) {
                if (StrUtil.isNotBlank(tag.trim())) {
                    skills.add(tag.trim());
                }
            }
        }

        if (StrUtil.isNotBlank(detail.getSkillsJson())) {
            try {
                JSONArray skillsArray = JSONUtil.parseArray(detail.getSkillsJson());
                for (Object obj : skillsArray) {
                    if (obj instanceof String) {
                        skills.add((String) obj);
                    } else if (obj instanceof JSONObject) {
                        JSONObject skillObj = (JSONObject) obj;
                        String skillName = skillObj.getStr("name", skillObj.getStr("skill"));
                        if (StrUtil.isNotBlank(skillName)) {
                            skills.add(skillName);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("解析技能JSON失败", e);
            }
        }

        analysis.set("totalSkills", skills.size());

        List<String> hotSkillsFound = skills.stream()
                .filter(skill -> HOT_SKILLS.stream().anyMatch(hot ->
                        skill.toLowerCase().contains(hot.toLowerCase()) ||
                        hot.toLowerCase().contains(skill.toLowerCase())))
                .collect(Collectors.toList());
        analysis.set("hotSkills", hotSkillsFound);

        JSONObject skillDistribution = new JSONObject();
        for (Map.Entry<String, List<String>> entry : SKILL_CATEGORIES.entrySet()) {
            List<String> categorySkills = skills.stream()
                    .filter(skill -> entry.getValue().stream().anyMatch(cat ->
                            skill.toLowerCase().contains(cat.toLowerCase()) ||
                            cat.toLowerCase().contains(skill.toLowerCase())))
                    .collect(Collectors.toList());
            if (!categorySkills.isEmpty()) {
                skillDistribution.set(entry.getKey(), categorySkills);
            }
        }
        analysis.set("skillDistribution", skillDistribution);

        return analysis.toString();
    }

    @Override
    public int calculateFormatScore(ResumeDetail detail) {
        int score = 100;

        if (StrUtil.isNotBlank(detail.getPhone()) && !detail.getPhone().matches("^1[3-9]\\d{9}$")) {
            score -= 5;
        }

        if (StrUtil.isNotBlank(detail.getEmail()) && !detail.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            score -= 5;
        }

        if (StrUtil.isNotBlank(detail.getSelfEvaluation())) {
            int length = detail.getSelfEvaluation().length();
            if (length < 50) score -= 10;
            else if (length > 500) score -= 5;
        }

        return Math.max(0, score);
    }

    @Override
    public AnalysisReport getLatestReport(Long resumeId) {
        return analysisReportService.getLatestByResumeId(resumeId);
    }

    @Override
    public String convertToText(ResumeDetail detail) {
        StringBuilder sb = new StringBuilder();
        
        // 基本信息
        sb.append("【基本信息】\n");
        if (StrUtil.isNotBlank(detail.getName())) sb.append("姓名：").append(detail.getName()).append("\n");
        if (detail.getGender() != null) sb.append("性别：").append(detail.getGender() == 1 ? "男" : "女").append("\n");
        if (detail.getAge() != null) sb.append("年龄：").append(detail.getAge()).append("岁\n");
        if (detail.getWorkYears() != null) sb.append("工作年限：").append(detail.getWorkYears()).append("年\n");
        if (StrUtil.isNotBlank(detail.getCurrentCity())) sb.append("所在城市：").append(detail.getCurrentCity()).append("\n");
        if (StrUtil.isNotBlank(detail.getHighestEducation())) sb.append("最高学历：").append(detail.getHighestEducation()).append("\n");
        if (StrUtil.isNotBlank(detail.getJobStatus())) sb.append("求职状态：").append(detail.getJobStatus()).append("\n");

        // 求职意向
        sb.append("\n【求职意向】\n");
        if (StrUtil.isNotBlank(detail.getExpectedPosition())) sb.append("期望职位：").append(detail.getExpectedPosition()).append("\n");
        if (StrUtil.isNotBlank(detail.getExpectedSalary())) sb.append("期望薪资：").append(detail.getExpectedSalary()).append("\n");
        if (StrUtil.isNotBlank(detail.getExpectedCity())) sb.append("期望城市：").append(detail.getExpectedCity()).append("\n");
        if (StrUtil.isNotBlank(detail.getJobType())) sb.append("工作类型：").append(detail.getJobType()).append("\n");

        // 教育经历
        if (StrUtil.isNotBlank(detail.getEducationJson()) && !"[]".equals(detail.getEducationJson())) {
            sb.append("\n【教育经历】\n");
            try {
                JSONArray eduArray = JSONUtil.parseArray(detail.getEducationJson());
                for (int i = 0; i < eduArray.size(); i++) {
                    JSONObject edu = eduArray.getJSONObject(i);
                    String startDate = edu.getStr("startDate", "");
                    String endDate = edu.getStr("endDate", "至今");
                    String school = edu.getStr("school", "");
                    String major = edu.getStr("major", "");
                    String degree = edu.getStr("degree", edu.getStr("education", ""));
                    
                    sb.append(startDate).append(" - ").append(endDate).append("\n");
                    sb.append(school);
                    if (StrUtil.isNotBlank(major)) sb.append(" | ").append(major);
                    if (StrUtil.isNotBlank(degree)) sb.append(" | ").append(degree);
                    sb.append("\n");
                    
                    String description = edu.getStr("description", "");
                    if (StrUtil.isNotBlank(description)) {
                        sb.append("描述：").append(description).append("\n");
                    }
                    sb.append("\n");
                }
            } catch (Exception e) {
                log.warn("解析教育经历JSON失败", e);
            }
        }

        // 工作经历
        if (StrUtil.isNotBlank(detail.getWorkExperienceJson()) && !"[]".equals(detail.getWorkExperienceJson())) {
            sb.append("\n【工作经历】\n");
            try {
                JSONArray workArray = JSONUtil.parseArray(detail.getWorkExperienceJson());
                for (int i = 0; i < workArray.size(); i++) {
                    JSONObject work = workArray.getJSONObject(i);
                    String startDate = work.getStr("startDate", "");
                    String endDate = work.getStr("endDate", "至今");
                    String company = work.getStr("company", "");
                    String position = work.getStr("position", "");
                    
                    sb.append(startDate).append(" - ").append(endDate).append("\n");
                    sb.append(company);
                    if (StrUtil.isNotBlank(position)) sb.append(" | ").append(position);
                    sb.append("\n");
                    
                    String responsibilities = work.getStr("responsibilities", work.getStr("description", ""));
                    if (StrUtil.isNotBlank(responsibilities)) {
                        sb.append("工作内容：").append(responsibilities).append("\n");
                    }
                    sb.append("\n");
                }
            } catch (Exception e) {
                log.warn("解析工作经历JSON失败", e);
            }
        }

        // 项目经验
        if (StrUtil.isNotBlank(detail.getProjectExperienceJson()) && !"[]".equals(detail.getProjectExperienceJson())) {
            sb.append("\n【项目经验】\n");
            try {
                JSONArray projArray = JSONUtil.parseArray(detail.getProjectExperienceJson());
                for (int i = 0; i < projArray.size(); i++) {
                    JSONObject proj = projArray.getJSONObject(i);
                    String startDate = proj.getStr("startDate", "");
                    String endDate = proj.getStr("endDate", "至今");
                    String name = proj.getStr("name", proj.getStr("projectName", ""));
                    String role = proj.getStr("role", "");
                    
                    sb.append(startDate).append(" - ").append(endDate).append("\n");
                    sb.append("项目名称：").append(name).append("\n");
                    if (StrUtil.isNotBlank(role)) sb.append("担任角色：").append(role).append("\n");
                    
                    String description = proj.getStr("description", "");
                    if (StrUtil.isNotBlank(description)) {
                        sb.append("项目描述：").append(description).append("\n");
                    }
                    
                    String technologies = proj.getStr("technologies", proj.getStr("techStack", ""));
                    if (StrUtil.isNotBlank(technologies)) {
                        sb.append("技术栈：").append(technologies).append("\n");
                    }
                    sb.append("\n");
                }
            } catch (Exception e) {
                log.warn("解析项目经验JSON失败", e);
            }
        }

        // 技能特长
        if (StrUtil.isNotBlank(detail.getSkillTags())) {
            sb.append("\n【技能特长】\n").append(detail.getSkillTags()).append("\n");
        } else if (StrUtil.isNotBlank(detail.getSkillsJson()) && !"[]".equals(detail.getSkillsJson())) {
            sb.append("\n【技能特长】\n");
            try {
                JSONArray skillsArray = JSONUtil.parseArray(detail.getSkillsJson());
                List<String> skillNames = new ArrayList<>();
                for (int i = 0; i < skillsArray.size(); i++) {
                    Object obj = skillsArray.get(i);
                    if (obj instanceof String) {
                        skillNames.add((String) obj);
                    } else if (obj instanceof JSONObject) {
                        JSONObject skillObj = (JSONObject) obj;
                        String skillName = skillObj.getStr("name", skillObj.getStr("skill", ""));
                        if (StrUtil.isNotBlank(skillName)) {
                            skillNames.add(skillName);
                        }
                    }
                }
                sb.append(String.join("、", skillNames)).append("\n");
            } catch (Exception e) {
                log.warn("解析技能JSON失败", e);
            }
        }

        // 自我评价
        if (StrUtil.isNotBlank(detail.getSelfEvaluation())) {
            sb.append("\n【自我评价】\n").append(detail.getSelfEvaluation()).append("\n");
        }

        // 个人优势
        if (StrUtil.isNotBlank(detail.getAdvantages())) {
            sb.append("\n【个人优势】\n").append(detail.getAdvantages()).append("\n");
        }

        return sb.toString();
    }

    private JSONObject parseJsonSafely(String jsonStr) {
        if (StrUtil.isBlank(jsonStr)) return null;
        try {
            int start = jsonStr.indexOf("{");
            int end = jsonStr.lastIndexOf("}");
            if (start >= 0 && end > start) {
                jsonStr = jsonStr.substring(start, end + 1);
            }
            return JSONUtil.parseObj(jsonStr);
        } catch (Exception e) {
            log.warn("解析JSON失败: {}", jsonStr, e);
            return null;
        }
    }

    private int calculateAverageScore(JSONObject qualityJson) {
        int total = 0;
        int count = 0;
        String[] scoreFields = {"professionalScore", "clarityScore", "completenessScore", "formatScore"};
        for (String field : scoreFields) {
            Integer score = qualityJson.getInt(field);
            if (score != null) {
                total += score;
                count++;
            }
        }
        return count > 0 ? total / count : 70;
    }

    private void generateDefaultSuggestions(AnalysisReport report, ResumeDetail detail) {
        List<String> suggestions = new ArrayList<>();
        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();

        if (StrUtil.isBlank(detail.getSelfEvaluation())) {
            suggestions.add("建议添加自我评价，突出个人优势和职业目标");
            weaknesses.add("缺少自我评价");
        } else if (detail.getSelfEvaluation().length() < 50) {
            suggestions.add("自我评价内容较少，建议丰富描述");
            weaknesses.add("自我评价内容较少");
        } else {
            strengths.add("有完整的自我评价");
        }

        if (StrUtil.isBlank(detail.getProjectExperienceJson()) || "[]".equals(detail.getProjectExperienceJson())) {
            suggestions.add("建议添加项目经验，展示实际工作能力");
            weaknesses.add("缺少项目经验");
        } else {
            strengths.add("有项目经验展示");
        }

        if (StrUtil.isBlank(detail.getSkillTags()) && StrUtil.isBlank(detail.getSkillsJson())) {
            suggestions.add("建议添加技能标签");
            weaknesses.add("缺少技能标签");
        } else {
            strengths.add("有技能标签展示");
        }

        if (detail.getWorkYears() != null && detail.getWorkYears() >= 3) {
            strengths.add("有" + detail.getWorkYears() + "年工作经验");
        }

        report.setSuggestions(JSONUtil.toJsonStr(suggestions));
        report.setStrengthPoints(JSONUtil.toJsonStr(strengths));
        report.setWeakPoints(JSONUtil.toJsonStr(weaknesses));
        report.setAiSuggestions("建议完善简历各模块内容，突出个人优势和项目经验");
    }

    private int calculateContentQualityByRules(ResumeDetail detail) {
        int score = 60;
        if (StrUtil.isNotBlank(detail.getSelfEvaluation())) {
            int length = detail.getSelfEvaluation().length();
            if (length >= 100 && length <= 300) score += 10;
            else if (length >= 50) score += 5;
        }
        if (StrUtil.isNotBlank(detail.getWorkExperienceJson()) && !"[]".equals(detail.getWorkExperienceJson())) {
            score += 15;
        }
        if (StrUtil.isNotBlank(detail.getProjectExperienceJson()) && !"[]".equals(detail.getProjectExperienceJson())) {
            score += 10;
        }
        return Math.min(100, score);
    }

    private String extractKeywordsByRules(ResumeDetail detail) {
        Set<String> keywords = new HashSet<>();
        if (StrUtil.isNotBlank(detail.getSkillTags())) {
            String[] tags = detail.getSkillTags().split("[,，、;；]");
            for (String tag : tags) {
                if (StrUtil.isNotBlank(tag.trim())) {
                    keywords.add(tag.trim());
                }
            }
        }
        if (StrUtil.isNotBlank(detail.getExpectedPosition())) {
            keywords.add(detail.getExpectedPosition());
        }
        if (StrUtil.isNotBlank(detail.getHighestEducation())) {
            keywords.add(detail.getHighestEducation());
        }
        return String.join(",", keywords);
    }

    private String calculateCompetitivenessByRules(int completeness, int quality, int format) {
        double avg = (completeness + quality + format) / 3.0;
        if (avg >= 85) return "优秀";
        if (avg >= 70) return "良好";
        if (avg >= 60) return "一般";
        return "需改进";
    }
}