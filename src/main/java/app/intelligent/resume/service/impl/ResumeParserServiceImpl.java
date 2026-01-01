package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.service.IResumeParserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历解析服务实现类
 * 使用Apache Tika提取文本，正则表达式提取结构化信息
 * 针对常见简历模板优化
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeParserServiceImpl implements IResumeParserService {

    private final Tika tika = new Tika();
    private final ObjectMapper objectMapper;

    // ========== 基本信息正则 ==========
    
    /** 姓名正则 - 支持多种格式 */
    private static final Pattern[] NAME_PATTERNS = {
        // 格式1：姓名：张三 或 姓 名：张三
        Pattern.compile("(?:姓\\s*名)[：:\\s]*([\\u4e00-\\u9fa5]{2,4})"),
        // 格式2：简历开头 "姓名 求职意向" 格式（如：冀旻 求职意向：Java开发工程师）
        // 支持空格、制表符、换行等各种分隔
        Pattern.compile("^\\s*([\\u4e00-\\u9fa5]{2,4})\\s*求职意向", Pattern.MULTILINE),
        // 格式3：文档开头的中文名后跟换行或年龄/电话等
        Pattern.compile("^\\s*([\\u4e00-\\u9fa5]{2,4})\\s*(?:\\n|\\r|年\\s*龄|电\\s*话)", Pattern.MULTILINE),
        // 格式4：文档开头单独一行的中文名（2-4字）
        Pattern.compile("^\\s*([\\u4e00-\\u9fa5]{2,4})\\s*$", Pattern.MULTILINE),
        // 格式5：文档最开头的2-4个汉字（兜底方案）
        Pattern.compile("^([\\u4e00-\\u9fa5]{2,4})"),
    };

    /** 手机号正则 */
    private static final Pattern[] PHONE_PATTERNS = {
        // 格式：电 话：182-4171-6773 或 电话：18241716773
        Pattern.compile("(?:电\\s*话|手机|联系电话|Tel)[：:\\s]*([1][3-9][0-9][-\\s]?[0-9]{4}[-\\s]?[0-9]{4})"),
        // 直接匹配11位手机号
        Pattern.compile("([1][3-9]\\d{9})"),
        // 带分隔符的手机号
        Pattern.compile("([1][3-9][0-9]-[0-9]{4}-[0-9]{4})"),
    };

    /** 邮箱正则 */
    private static final Pattern[] EMAIL_PATTERNS = {
        // 格式：邮 箱：xxx@163.com
        Pattern.compile("(?:邮\\s*箱|Email|E-mail|电子邮箱)[：:\\s]*([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
        // 直接匹配邮箱
        Pattern.compile("([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
    };

    /** 年龄正则 */
    private static final Pattern[] AGE_PATTERNS = {
        // 格式：年 龄：21岁 或 年龄：21
        Pattern.compile("(?:年\\s*龄)[：:\\s]*(\\d{1,2})\\s*岁?"),
        // 格式：21岁
        Pattern.compile("(\\d{1,2})\\s*岁"),
    };

    /** 学历正则 */
    private static final Pattern[] EDUCATION_PATTERNS = {
        // 格式：学 历：本科学士学位
        Pattern.compile("(?:学\\s*历|最高学历)[：:\\s]*(博士|硕士|研究生|本科|大专|专科|高中|中专)[^\\n]*"),
        // 格式：本科学士学位
        Pattern.compile("(博士|硕士研究生|硕士|研究生|本科学士|本科|大专|专科)(?:学位|学历)?"),
    };

    /** 求职意向正则 */
    private static final Pattern[] POSITION_PATTERNS = {
        // 格式：求职意向：Java开发工程师
        Pattern.compile("(?:求职意向|期望职位|应聘职位|目标职位)[：:\\s]*([^\\n\\r]{2,30})"),
        // 格式：姓名 求职意向：Java开发工程师
        Pattern.compile("求职意向[：:\\s]*([\\u4e00-\\u9fa5a-zA-Z]+(?:工程师|开发|设计师|经理|专员))"),
    };


    /** 英语等级正则 */
    private static final Pattern ENGLISH_LEVEL_PATTERN = Pattern.compile(
            "(?:英语等级|英语水平|外语水平)[：:\\s]*(CET-?[46]|六级|四级|雅思\\d+\\.?\\d*|托福\\d+)");

    /** 专业成绩正则 */
    private static final Pattern GPA_PATTERN = Pattern.compile(
            "(?:专业成绩|GPA|绩点)[：:\\s]*([\\d.]+[/／][\\d.]+|[\\d.]+)");

    // ========== 教育背景正则 ==========
    
    /** 教育经历正则 - 匹配：时间段 学校 专业 */
    private static final Pattern EDUCATION_ENTRY_PATTERN = Pattern.compile(
            "(\\d{4}[./-]\\d{1,2})\\s*[-—~至到]+\\s*(\\d{4}[./-]\\d{1,2}|至今)\\s+" +
            "([\\u4e00-\\u9fa5]+(?:大学|学院|学校))\\s+" +
            "([\\u4e00-\\u9fa5a-zA-Z]+(?:专业|工程|科学|技术|与[\\u4e00-\\u9fa5]+)?)"
    );

    // ========== 工作/实习经历正则 ==========
    
    /** 工作经历正则 - 匹配：时间段 公司 职位 */
    private static final Pattern WORK_ENTRY_PATTERN = Pattern.compile(
            "(\\d{4}[./-]\\d{1,2})\\s*[-—~至到]+\\s*(\\d{4}[./-]\\d{1,2}|至今)\\s+" +
            "([\\u4e00-\\u9fa5a-zA-Z]+(?:公司|集团|科技|软件|网络|有限|股份)[\\u4e00-\\u9fa5]*)\\s+" +
            "([\\u4e00-\\u9fa5a-zA-Z]+(?:开发|工程师|设计|运营|经理|专员|实习生)?)"
    );

    // ========== 项目经历正则 ==========
    
    /** 项目经历正则 - 匹配：时间段 项目名 角色 */
    private static final Pattern PROJECT_ENTRY_PATTERN = Pattern.compile(
            "(\\d{4}[./-]\\d{1,2})\\s*[-—~至到]+\\s*(\\d{4}[./-]\\d{1,2}|至今)\\s+" +
            "([\\u4e00-\\u9fa5a-zA-Z0-9]+(?:系统|平台|项目|APP|网站|服务|中枢|创作)[\\u4e00-\\u9fa5a-zA-Z0-9]*)\\s*" +
            "([\\u4e00-\\u9fa5a-zA-Z]*(?:开发|工程师|设计|负责人|成员)?)?"
    );

    // ========== 技能正则 ==========
    
    /** 技能关键词列表 */
    private static final String[] SKILL_KEYWORDS = {
        "Java", "Spring", "SpringBoot", "SpringMVC", "SpringCloud", "MyBatis", "MybatisPlus",
        "MySQL", "PostgreSQL", "Redis", "ElasticSearch", "MongoDB",
        "Vue", "React", "JavaScript", "TypeScript", "HTML", "CSS", "jQuery",
        "Docker", "Kubernetes", "Nginx", "Linux", "Git",
        "RabbitMQ", "RocketMQ", "Kafka", "Netty", "WebSocket",
        "Gradle", "Maven", "IDEA", "Nacos", "Gateway", "Sentinel", "Feign",
        "Redisson", "JWT", "MD5", "ECharts", "Milvus", "Ollama", "Qwen", "DeepSeek",
        "Ruoyi", "Ruoyi-Boot", "Ruoyi-Cloud", "SpringCloudAlibaba"
    };

    @Override
    public ResumeDetail parseResume(InputStream inputStream, String fileName) {
        try {
            // 1. 使用Tika提取文本
            String content = extractText(inputStream, fileName);
            if (!StringUtils.hasText(content)) {
                log.warn("简历文件内容为空: {}", fileName);
                return new ResumeDetail();
            }

            log.info("【简历解析】提取的文本长度: {}", content.length());
            log.info("【简历解析】文本前500字符:\n{}", content.substring(0, Math.min(500, content.length())));

            // 2. 解析结构化信息
            ResumeDetail detail = new ResumeDetail();

            // 基本信息
            detail.setName(extractWithPatterns(content, NAME_PATTERNS));
            detail.setPhone(cleanPhone(extractWithPatterns(content, PHONE_PATTERNS)));
            detail.setEmail(extractWithPatterns(content, EMAIL_PATTERNS));
            detail.setGender(extractGender(content));
            detail.setAge(extractAge(content));
            detail.setHighestEducation(extractEducationLevel(content));

            // 求职意向
            detail.setExpectedPosition(extractWithPatterns(content, POSITION_PATTERNS));
            detail.setExpectedCity(extractCity(content, "期望"));
            detail.setCurrentCity(extractCity(content, "现居|所在"));
            detail.setJobStatus(extractJobStatus(content));
            detail.setWorkYears(extractWorkYears(content));

            // 技能标签 - 重要！
            detail.setSkillTags(extractSkillTags(content));
            detail.setSkillsJson(extractSkillsJson(content));

            // 自我评价
            detail.setSelfEvaluation(extractSelfEvaluation(content));

            // 教育经历
            detail.setEducationJson(extractEducationJson(content));

            // 工作/实习经历
            detail.setWorkExperienceJson(extractWorkExperienceJson(content));

            // 项目经验
            detail.setProjectExperienceJson(extractProjectExperienceJson(content));

            // 校园经历（存入extraInfo）
            detail.setExtraInfo(extractCampusExperience(content));

            log.info("简历解析完成: name={}, phone={}, email={}, skills={}", 
                    detail.getName(), detail.getPhone(), detail.getEmail(), detail.getSkillTags());

            return detail;
        } catch (Exception e) {
            log.error("简历解析失败: {}", fileName, e);
            throw new BusinessException(ResultCode.RESUME_PARSE_FAILED);
        }
    }

    @Override
    public ResumeDetail parseResumeFromUrl(String fileUrl, String fileName) {
        try {
            URL url = new URL(fileUrl);
            try (InputStream inputStream = url.openStream()) {
                return parseResume(inputStream, fileName);
            }
        } catch (IOException e) {
            log.error("从URL读取简历失败: {}", fileUrl, e);
            throw new BusinessException(ResultCode.RESUME_PARSE_FAILED);
        }
    }

    @Override
    public String extractText(InputStream inputStream, String fileName) {
        try {
            tika.setMaxStringLength(10 * 1024 * 1024); // 10MB
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            log.error("Tika提取文本失败: {}", fileName, e);
            throw new BusinessException(ResultCode.RESUME_PARSE_FAILED);
        }
    }


    // ========== 提取方法 ==========

    /**
     * 使用多个正则模式提取，返回第一个匹配结果
     */
    private String extractWithPatterns(String content, Pattern[] patterns) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String result = matcher.group(1).trim();
                if (StringUtils.hasText(result) && !isCommonWord(result)) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * 清理手机号（去除分隔符）
     */
    private String cleanPhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[-\\s]", "");
    }

    /**
     * 判断是否为常见非姓名词汇
     */
    private boolean isCommonWord(String word) {
        String[] commonWords = {"简历", "个人", "求职", "应聘", "基本", "信息", "联系", "方式", 
                "教育", "背景", "技能", "经历", "项目", "工作", "实习", "自我", "评价"};
        for (String common : commonWords) {
            if (word.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取性别
     */
    private Integer extractGender(String content) {
        Pattern pattern = Pattern.compile("(?:性\\s*别)[：:\\s]*(男|女)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return "男".equals(matcher.group(1)) ? 1 : 0;
        }
        return null;
    }

    /**
     * 提取年龄
     */
    private Integer extractAge(String content) {
        for (Pattern pattern : AGE_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    return Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * 提取学历等级
     */
    private String extractEducationLevel(String content) {
        for (Pattern pattern : EDUCATION_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                String edu = matcher.group(1);
                // 标准化学历
                if (edu.contains("博士")) return "博士";
                if (edu.contains("硕士") || edu.contains("研究生")) return "硕士";
                if (edu.contains("本科")) return "本科";
                if (edu.contains("大专") || edu.contains("专科")) return "大专";
                return edu;
            }
        }
        // 直接搜索关键词
        String[] educations = {"博士", "硕士", "研究生", "本科", "大专", "专科"};
        for (String edu : educations) {
            if (content.contains(edu)) {
                return edu.equals("研究生") ? "硕士" : edu;
            }
        }
        return null;
    }

    /**
     * 提取城市
     */
    private String extractCity(String content, String prefix) {
        Pattern pattern = Pattern.compile("(?:" + prefix + ")(?:城市|地点|地址)?[：:\\s]*([\\u4e00-\\u9fa5]{2,10})");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    /**
     * 提取求职状态
     */
    private String extractJobStatus(String content) {
        Pattern pattern = Pattern.compile("(?:求职状态|目前状态)[：:\\s]*(在职|离职|应届|在校|随时到岗|一周内到岗|一个月内到岗)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 根据内容推断
        if (content.contains("应届") || content.contains("在校")) {
            return "应届";
        }
        return null;
    }

    /**
     * 提取工作年限
     */
    private Integer extractWorkYears(String content) {
        Pattern pattern = Pattern.compile("(?:工作年限|工作经验|从业)[：:\\s]*(\\d{1,2})\\s*年");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // 应届生默认0年
        if (content.contains("应届") || content.contains("在校")) {
            return 0;
        }
        return null;
    }

    /**
     * 提取技能标签（核心方法）
     */
    private String extractSkillTags(String content) {
        Set<String> skills = new LinkedHashSet<>();
        String upperContent = content.toUpperCase();
        
        // 1. 匹配预定义的技能关键词
        for (String skill : SKILL_KEYWORDS) {
            if (upperContent.contains(skill.toUpperCase())) {
                skills.add(skill);
            }
        }
        
        // 2. 从"个人技能"区块提取
        Pattern skillSectionPattern = Pattern.compile(
                "(?:个人技能|专业技能|技术栈|技能特长)[\\s\\S]*?(?=●|•|教育|实习|项目|工作|自我|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher sectionMatcher = skillSectionPattern.matcher(content);
        if (sectionMatcher.find()) {
            String skillSection = sectionMatcher.group();
            // 提取技能关键词
            extractSkillsFromSection(skillSection, skills);
        }
        
        // 3. 从项目描述中提取技术栈
        Pattern techPattern = Pattern.compile(
                "(?:基于|使用|运用|采用|通过)\\s*([A-Za-z][A-Za-z0-9+#.-]*(?:\\s*[+,、]\\s*[A-Za-z][A-Za-z0-9+#.-]*)*)");
        Matcher techMatcher = techPattern.matcher(content);
        while (techMatcher.find()) {
            String techs = techMatcher.group(1);
            String[] techArray = techs.split("[+,、\\s]+");
            for (String tech : techArray) {
                tech = tech.trim();
                if (tech.length() >= 2 && tech.length() <= 20) {
                    skills.add(tech);
                }
            }
        }
        
        return skills.isEmpty() ? null : String.join(",", skills);
    }

    /**
     * 从技能区块提取技能
     */
    private void extractSkillsFromSection(String section, Set<String> skills) {
        // 匹配"熟悉/熟练/掌握/了解 XXX"格式
        Pattern pattern = Pattern.compile(
                "(?:熟悉|熟练|掌握|精通|了解|使用)\\s*([A-Za-z][A-Za-z0-9+#./-]*(?:[、,，]\\s*[A-Za-z][A-Za-z0-9+#./-]*)*)");
        Matcher matcher = pattern.matcher(section);
        while (matcher.find()) {
            String skillStr = matcher.group(1);
            String[] skillArray = skillStr.split("[、,，\\s]+");
            for (String skill : skillArray) {
                skill = skill.trim();
                if (skill.length() >= 2 && skill.length() <= 30 && skill.matches(".*[A-Za-z].*")) {
                    skills.add(skill);
                }
            }
        }
    }


    /**
     * 提取技能JSON
     */
    private String extractSkillsJson(String content) {
        List<Map<String, Object>> skillList = new ArrayList<>();
        
        // 匹配"熟悉/熟练/掌握/了解 XXX"格式
        Pattern pattern = Pattern.compile(
                "(熟练|熟悉|精通|掌握|了解)(?:使用|运用)?\\s*([^；;。\\n]+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String level = matcher.group(1);
            String skillStr = matcher.group(2).trim();
            
            // 清理技能字符串
            skillStr = skillStr.replaceAll("[，,]\\s*$", "");
            
            if (skillStr.length() > 5 && skillStr.length() < 200) {
                Map<String, Object> skillItem = new HashMap<>();
                skillItem.put("level", level);
                skillItem.put("description", skillStr);
                skillList.add(skillItem);
            }
        }
        
        try {
            return skillList.isEmpty() ? null : objectMapper.writeValueAsString(skillList);
        } catch (JsonProcessingException e) {
            log.warn("技能JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 提取自我评价
     */
    private String extractSelfEvaluation(String content) {
        // 匹配自我评价区块
        Pattern pattern = Pattern.compile(
                "(?:●\\s*)?(?:自我评价|个人简介|自我介绍)[\\s\\S]*?(?=●|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String evaluation = matcher.group()
                    .replaceFirst("(?:●\\s*)?(?:自我评价|个人简介|自我介绍)[：:\\s]*", "")
                    .trim();
            // 清理多余空白
            evaluation = evaluation.replaceAll("\\s+", " ");
            if (evaluation.length() > 10) {
                return evaluation;
            }
        }
        return null;
    }

    /**
     * 提取教育经历JSON
     */
    private String extractEducationJson(String content) {
        List<Map<String, Object>> eduList = new ArrayList<>();
        
        // 查找教育背景区块
        Pattern sectionPattern = Pattern.compile(
                "(?:●\\s*)?(?:教育背景|教育经历|学历)[\\s\\S]*?(?=●\\s*(?:个人技能|实习|工作|项目|自我)|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher sectionMatcher = sectionPattern.matcher(content);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group();
            
            // 匹配教育条目：时间 学校 专业
            Matcher entryMatcher = EDUCATION_ENTRY_PATTERN.matcher(section);
            while (entryMatcher.find()) {
                Map<String, Object> edu = new HashMap<>();
                edu.put("startDate", entryMatcher.group(1));
                edu.put("endDate", entryMatcher.group(2));
                edu.put("school", entryMatcher.group(3));
                edu.put("major", entryMatcher.group(4));
                eduList.add(edu);
            }
            
            // 提取主修课程
            Pattern coursePattern = Pattern.compile("主修课程[：:\\s]*([^\\n]+)");
            Matcher courseMatcher = coursePattern.matcher(section);
            if (courseMatcher.find() && !eduList.isEmpty()) {
                eduList.get(0).put("courses", courseMatcher.group(1).trim());
            }
            
            // 提取校内荣誉
            Pattern honorPattern = Pattern.compile("校内荣誉[：:\\s]*([^\\n]+)");
            Matcher honorMatcher = honorPattern.matcher(section);
            if (honorMatcher.find() && !eduList.isEmpty()) {
                eduList.get(0).put("honors", honorMatcher.group(1).trim());
            }
            
            // 提取专业成绩
            Matcher gpaMatcher = GPA_PATTERN.matcher(section);
            if (gpaMatcher.find() && !eduList.isEmpty()) {
                eduList.get(0).put("gpa", gpaMatcher.group(1));
            }
        }
        
        try {
            return eduList.isEmpty() ? null : objectMapper.writeValueAsString(eduList);
        } catch (JsonProcessingException e) {
            log.warn("教育经历JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 提取工作/实习经历JSON
     */
    private String extractWorkExperienceJson(String content) {
        List<Map<String, Object>> workList = new ArrayList<>();
        
        // 查找实习经历区块
        Pattern sectionPattern = Pattern.compile(
                "(?:●\\s*)?(?:实习经历|工作经历|工作经验)[\\s\\S]*?(?=●\\s*(?:项目|校园|自我|教育)|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher sectionMatcher = sectionPattern.matcher(content);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group();
            
            // 匹配工作条目
            Matcher entryMatcher = WORK_ENTRY_PATTERN.matcher(section);
            while (entryMatcher.find()) {
                Map<String, Object> work = new HashMap<>();
                work.put("startDate", entryMatcher.group(1));
                work.put("endDate", entryMatcher.group(2));
                work.put("company", entryMatcher.group(3));
                work.put("position", entryMatcher.group(4));
                
                // 提取工作描述
                int endPos = entryMatcher.end();
                String remaining = section.substring(endPos);
                Pattern descPattern = Pattern.compile("工作描述[：:\\s]*([\\s\\S]*?)(?=\\d{4}[./-]|$)");
                Matcher descMatcher = descPattern.matcher(remaining);
                if (descMatcher.find()) {
                    work.put("description", cleanDescription(descMatcher.group(1)));
                }
                
                workList.add(work);
            }
        }
        
        try {
            return workList.isEmpty() ? null : objectMapper.writeValueAsString(workList);
        } catch (JsonProcessingException e) {
            log.warn("工作经历JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 提取项目经验JSON
     */
    private String extractProjectExperienceJson(String content) {
        List<Map<String, Object>> projectList = new ArrayList<>();
        
        // 查找实践经历/项目经历区块
        Pattern sectionPattern = Pattern.compile(
                "(?:●\\s*)?(?:实践经历|项目经历|项目经验)[\\s\\S]*?(?=●\\s*(?:校园|自我|教育|实习)|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher sectionMatcher = sectionPattern.matcher(content);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group();
            
            // 匹配项目条目
            Matcher entryMatcher = PROJECT_ENTRY_PATTERN.matcher(section);
            int lastEnd = 0;
            Map<String, Object> currentProject = null;
            
            while (entryMatcher.find()) {
                // 保存上一个项目的描述
                if (currentProject != null && lastEnd > 0) {
                    String desc = section.substring(lastEnd, entryMatcher.start()).trim();
                    if (desc.length() > 10) {
                        currentProject.put("description", cleanDescription(desc));
                    }
                }
                
                currentProject = new HashMap<>();
                currentProject.put("startDate", entryMatcher.group(1));
                currentProject.put("endDate", entryMatcher.group(2));
                currentProject.put("projectName", entryMatcher.group(3));
                if (entryMatcher.group(4) != null && !entryMatcher.group(4).isEmpty()) {
                    currentProject.put("role", entryMatcher.group(4));
                }
                
                projectList.add(currentProject);
                lastEnd = entryMatcher.end();
            }
            
            // 处理最后一个项目的描述
            if (currentProject != null && lastEnd > 0 && lastEnd < section.length()) {
                String desc = section.substring(lastEnd).trim();
                if (desc.length() > 10) {
                    currentProject.put("description", cleanDescription(desc));
                }
            }
            
            // 提取项目介绍和个人职责
            for (Map<String, Object> project : projectList) {
                extractProjectDetails(section, project);
            }
        }
        
        try {
            return projectList.isEmpty() ? null : objectMapper.writeValueAsString(projectList);
        } catch (JsonProcessingException e) {
            log.warn("项目经验JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 提取项目详情（项目介绍、个人职责）
     */
    private void extractProjectDetails(String section, Map<String, Object> project) {
        String projectName = (String) project.get("projectName");
        if (projectName == null) return;
        
        // 查找该项目的详细描述区域
        int nameIndex = section.indexOf(projectName);
        if (nameIndex < 0) return;
        
        String projectSection = section.substring(nameIndex);
        // 截取到下一个项目或区块结束
        int nextProjectIndex = projectSection.indexOf("\n20", 10); // 下一个时间开头
        if (nextProjectIndex > 0) {
            projectSection = projectSection.substring(0, nextProjectIndex);
        }
        
        // 提取项目介绍
        Pattern introPattern = Pattern.compile("项目介绍[：:\\s]*([^●\\n][\\s\\S]*?)(?=个人职责|$)");
        Matcher introMatcher = introPattern.matcher(projectSection);
        if (introMatcher.find()) {
            project.put("introduction", cleanDescription(introMatcher.group(1)));
        }
        
        // 提取个人职责
        Pattern dutyPattern = Pattern.compile("个人职责[：:\\s]*([\\s\\S]*?)(?=\\d{4}[./-]|●|$)");
        Matcher dutyMatcher = dutyPattern.matcher(projectSection);
        if (dutyMatcher.find()) {
            project.put("duties", cleanDescription(dutyMatcher.group(1)));
        }
    }


    /**
     * 提取校园经历
     */
    private String extractCampusExperience(String content) {
        List<Map<String, Object>> campusList = new ArrayList<>();
        
        // 查找校园经历区块
        Pattern sectionPattern = Pattern.compile(
                "(?:●\\s*)?校园经历[\\s\\S]*?(?=●\\s*(?:自我|项目|实习|工作)|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher sectionMatcher = sectionPattern.matcher(content);
        
        if (sectionMatcher.find()) {
            String section = sectionMatcher.group();
            
            // 匹配校园经历条目：时间 组织/活动 角色
            Pattern entryPattern = Pattern.compile(
                    "(\\d{4}[./-]\\d{1,2})\\s*[-—~至到]+\\s*(\\d{4}[./-]\\d{1,2}|至今)\\s+" +
                    "([\\u4e00-\\u9fa5]+(?:大学|学院|协会|社团|大赛|比赛)[\\u4e00-\\u9fa5]*)\\s*" +
                    "([\\u4e00-\\u9fa5]+(?:成员|部长|主席|负责人|干事)?)?");
            
            Matcher entryMatcher = entryPattern.matcher(section);
            while (entryMatcher.find()) {
                Map<String, Object> campus = new HashMap<>();
                campus.put("startDate", entryMatcher.group(1));
                campus.put("endDate", entryMatcher.group(2));
                campus.put("organization", entryMatcher.group(3));
                if (entryMatcher.group(4) != null) {
                    campus.put("role", entryMatcher.group(4));
                }
                campusList.add(campus);
            }
        }
        
        try {
            return campusList.isEmpty() ? null : objectMapper.writeValueAsString(campusList);
        } catch (JsonProcessingException e) {
            log.warn("校园经历JSON序列化失败", e);
            return null;
        }
    }

    /**
     * 清理描述文本
     */
    private String cleanDescription(String text) {
        if (text == null) return null;
        return text
                .replaceAll("^[●•\\-\\s]+", "") // 去除开头的符号
                .replaceAll("[●•]", "\n") // 将项目符号转为换行
                .replaceAll("\\s+", " ") // 合并空白
                .replaceAll("\\n\\s+", "\n") // 清理换行后的空白
                .trim();
    }
}
