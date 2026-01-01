package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.service.IResumeParserService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历解析服务实现类
 * 使用Apache Tika提取文本，正则表达式提取结构化信息
 * 对应需求文档4.4简历解析模块
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
public class ResumeParserServiceImpl implements IResumeParserService {

    private final Tika tika = new Tika();

    // ========== 正则表达式模式 ==========

    /** 手机号正则：1开头的11位数字 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    /** 姓名正则：2-4个中文字符（简单匹配） */
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "(?:姓\\s*名|本人|我)[：:]*\\s*([\\u4e00-\\u9fa5]{2,4})");

    /** 姓名备选正则：文档开头的2-4个中文字符 */
    private static final Pattern NAME_FALLBACK_PATTERN = Pattern.compile(
            "^\\s*([\\u4e00-\\u9fa5]{2,4})\\s*$", Pattern.MULTILINE);

    /** 性别正则 */
    private static final Pattern GENDER_PATTERN = Pattern.compile(
            "(?:性\\s*别)[：:]*\\s*(男|女)");

    /** 年龄正则 */
    private static final Pattern AGE_PATTERN = Pattern.compile(
            "(?:年\\s*龄)[：:]*\\s*(\\d{1,2})\\s*岁?");

    /** 出生日期正则 */
    private static final Pattern BIRTH_DATE_PATTERN = Pattern.compile(
            "(?:出生日期|生日|出生年月)[：:]*\\s*(\\d{4}[-./年]\\d{1,2}[-./月]?\\d{0,2}日?)");

    /** 工作年限正则 */
    private static final Pattern WORK_YEARS_PATTERN = Pattern.compile(
            "(?:工作年限|工作经验|从业)[：:]*\\s*(\\d{1,2})\\s*年");

    /** 学历正则 */
    private static final Pattern EDUCATION_PATTERN = Pattern.compile(
            "(?:学\\s*历|最高学历)[：:]*\\s*(博士|硕士|研究生|本科|大专|专科|高中|中专)");

    /** 期望职位正则 */
    private static final Pattern EXPECTED_POSITION_PATTERN = Pattern.compile(
            "(?:期望职位|求职意向|应聘职位|目标职位)[：:]*\\s*([^\\n\\r]{2,30})");

    /** 期望薪资正则 */
    private static final Pattern EXPECTED_SALARY_PATTERN = Pattern.compile(
            "(?:期望薪资|期望月薪|薪资要求)[：:]*\\s*([\\d.]+[kK万]?\\s*[-~至到]\\s*[\\d.]+[kK万]?|面议|\\d+[kK万]以上)");

    /** 期望城市正则 */
    private static final Pattern EXPECTED_CITY_PATTERN = Pattern.compile(
            "(?:期望城市|工作地点|期望地点)[：:]*\\s*([\\u4e00-\\u9fa5]{2,10})");

    /** 当前城市正则 */
    private static final Pattern CURRENT_CITY_PATTERN = Pattern.compile(
            "(?:现居住地|所在城市|现居城市|居住地)[：:]*\\s*([\\u4e00-\\u9fa5]{2,10})");

    /** 求职状态正则 */
    private static final Pattern JOB_STATUS_PATTERN = Pattern.compile(
            "(?:求职状态|目前状态)[：:]*\\s*(在职|离职|应届|在校|随时到岗|一周内到岗|一个月内到岗)");

    /** 技能正则 */
    private static final Pattern SKILLS_PATTERN = Pattern.compile(
            "(?:技能|专业技能|技术栈|掌握技术)[：:]*\\s*([^\\n\\r]{5,200})");

    /** 自我评价正则 */
    private static final Pattern SELF_EVALUATION_PATTERN = Pattern.compile(
            "(?:自我评价|个人简介|自我介绍|个人评价)[：:]*\\s*([\\s\\S]{10,500}?)(?=\\n\\n|教育|工作|项目|$)");

    /** 教育经历区块正则 */
    private static final Pattern EDUCATION_SECTION_PATTERN = Pattern.compile(
            "(?:教育经历|教育背景|学历)[\\s\\S]*?(?=工作经历|工作经验|项目经验|技能|自我评价|$)",
            Pattern.CASE_INSENSITIVE);

    /** 工作经历区块正则 */
    private static final Pattern WORK_SECTION_PATTERN = Pattern.compile(
            "(?:工作经历|工作经验)[\\s\\S]*?(?=项目经验|教育经历|技能|自我评价|$)",
            Pattern.CASE_INSENSITIVE);

    /** 项目经验区块正则 */
    private static final Pattern PROJECT_SECTION_PATTERN = Pattern.compile(
            "(?:项目经验|项目经历)[\\s\\S]*?(?=教育经历|工作经历|技能|自我评价|$)",
            Pattern.CASE_INSENSITIVE);

    @Override
    public ResumeDetail parseResume(InputStream inputStream, String fileName) {
        try {
            // 1. 使用Tika提取文本
            String content = extractText(inputStream, fileName);
            if (!StringUtils.hasText(content)) {
                log.warn("简历文件内容为空: {}", fileName);
                return new ResumeDetail();
            }

            log.debug("提取的简历文本内容长度: {}", content.length());

            // 2. 解析结构化信息
            ResumeDetail detail = new ResumeDetail();

            // 基本信息
            detail.setName(extractName(content));
            detail.setPhone(extractPhone(content));
            detail.setEmail(extractEmail(content));
            detail.setGender(extractGender(content));
            detail.setAge(extractAge(content));
            detail.setBirthDate(extractBirthDate(content));

            // 求职意向
            detail.setWorkYears(extractWorkYears(content));
            detail.setHighestEducation(extractEducation(content));
            detail.setExpectedPosition(extractExpectedPosition(content));
            detail.setExpectedSalary(extractExpectedSalary(content));
            detail.setExpectedCity(extractExpectedCity(content));
            detail.setCurrentCity(extractCurrentCity(content));
            detail.setJobStatus(extractJobStatus(content));

            // 技能和自我评价
            String skills = extractSkills(content);
            if (StringUtils.hasText(skills)) {
                detail.setSkillTags(skills);
            }
            detail.setSelfEvaluation(extractSelfEvaluation(content));

            // 教育经历、工作经历、项目经验（JSON格式存储）
            detail.setEducationJson(extractEducationSection(content));
            detail.setWorkExperienceJson(extractWorkSection(content));
            detail.setProjectExperienceJson(extractProjectSection(content));

            log.info("简历解析完成: name={}, phone={}, email={}", 
                    detail.getName(), detail.getPhone(), detail.getEmail());

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
            // 设置最大文本长度限制
            tika.setMaxStringLength(10 * 1024 * 1024); // 10MB
            return tika.parseToString(inputStream);
        } catch (IOException | TikaException e) {
            log.error("Tika提取文本失败: {}", fileName, e);
            throw new BusinessException(ResultCode.RESUME_PARSE_FAILED);
        }
    }

    // ========== 私有提取方法 ==========

    /**
     * 提取姓名
     */
    private String extractName(String content) {
        // 先尝试匹配"姓名："格式
        Matcher matcher = NAME_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 备选：尝试匹配文档开头的中文名
        Matcher fallbackMatcher = NAME_FALLBACK_PATTERN.matcher(content);
        if (fallbackMatcher.find()) {
            String name = fallbackMatcher.group(1).trim();
            // 排除常见的非姓名词汇
            if (!isCommonWord(name)) {
                return name;
            }
        }

        return null;
    }

    /**
     * 判断是否为常见非姓名词汇
     */
    private boolean isCommonWord(String word) {
        String[] commonWords = {"简历", "个人", "求职", "应聘", "基本", "信息", "联系", "方式"};
        for (String common : commonWords) {
            if (word.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 提取手机号
     */
    private String extractPhone(String content) {
        Matcher matcher = PHONE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 提取邮箱
     */
    private String extractEmail(String content) {
        Matcher matcher = EMAIL_PATTERN.matcher(content);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * 提取性别
     */
    private Integer extractGender(String content) {
        Matcher matcher = GENDER_PATTERN.matcher(content);
        if (matcher.find()) {
            return "男".equals(matcher.group(1)) ? 1 : 0;
        }
        // 尝试直接匹配
        if (content.contains("男") && !content.contains("女")) {
            return 1;
        }
        if (content.contains("女") && !content.contains("男")) {
            return 0;
        }
        return null;
    }

    /**
     * 提取年龄
     */
    private Integer extractAge(String content) {
        Matcher matcher = AGE_PATTERN.matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 提取出生日期
     */
    private LocalDate extractBirthDate(String content) {
        Matcher matcher = BIRTH_DATE_PATTERN.matcher(content);
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            try {
                // 尝试多种日期格式
                dateStr = dateStr.replaceAll("[年月]", "-").replaceAll("日", "");
                if (dateStr.endsWith("-")) {
                    dateStr = dateStr.substring(0, dateStr.length() - 1);
                }
                
                // 补全日期
                String[] parts = dateStr.split("-");
                if (parts.length == 2) {
                    dateStr = dateStr + "-01";
                }
                
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-M-d"));
            } catch (Exception e) {
                log.debug("日期解析失败: {}", dateStr);
            }
        }
        return null;
    }

    /**
     * 提取工作年限
     */
    private Integer extractWorkYears(String content) {
        Matcher matcher = WORK_YEARS_PATTERN.matcher(content);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 提取学历
     */
    private String extractEducation(String content) {
        Matcher matcher = EDUCATION_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // 直接搜索学历关键词
        String[] educations = {"博士", "硕士", "研究生", "本科", "大专", "专科", "高中", "中专"};
        for (String edu : educations) {
            if (content.contains(edu)) {
                return edu;
            }
        }
        return null;
    }

    /**
     * 提取期望职位
     */
    private String extractExpectedPosition(String content) {
        Matcher matcher = EXPECTED_POSITION_PATTERN.matcher(content);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取期望薪资
     */
    private String extractExpectedSalary(String content) {
        Matcher matcher = EXPECTED_SALARY_PATTERN.matcher(content);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取期望城市
     */
    private String extractExpectedCity(String content) {
        Matcher matcher = EXPECTED_CITY_PATTERN.matcher(content);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取当前城市
     */
    private String extractCurrentCity(String content) {
        Matcher matcher = CURRENT_CITY_PATTERN.matcher(content);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取求职状态
     */
    private String extractJobStatus(String content) {
        Matcher matcher = JOB_STATUS_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 提取技能
     */
    private String extractSkills(String content) {
        Matcher matcher = SKILLS_PATTERN.matcher(content);
        if (matcher.find()) {
            String skills = matcher.group(1);
            // 清理并格式化技能列表
            skills = skills.replaceAll("[，、；;]", ",")
                    .replaceAll("\\s+", ",")
                    .replaceAll(",+", ",");
            if (skills.startsWith(",")) {
                skills = skills.substring(1);
            }
            if (skills.endsWith(",")) {
                skills = skills.substring(0, skills.length() - 1);
            }
            return skills;
        }
        return null;
    }

    /**
     * 提取自我评价
     */
    private String extractSelfEvaluation(String content) {
        Matcher matcher = SELF_EVALUATION_PATTERN.matcher(content);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取教育经历区块（返回原始文本，后续可转JSON）
     */
    private String extractEducationSection(String content) {
        Matcher matcher = EDUCATION_SECTION_PATTERN.matcher(content);
        if (matcher.find()) {
            String section = matcher.group();
            // 简单解析为JSON数组格式
            return parseEducationToJson(section);
        }
        return null;
    }

    /**
     * 提取工作经历区块
     */
    private String extractWorkSection(String content) {
        Matcher matcher = WORK_SECTION_PATTERN.matcher(content);
        if (matcher.find()) {
            String section = matcher.group();
            return parseWorkExperienceToJson(section);
        }
        return null;
    }

    /**
     * 提取项目经验区块
     */
    private String extractProjectSection(String content) {
        Matcher matcher = PROJECT_SECTION_PATTERN.matcher(content);
        if (matcher.find()) {
            String section = matcher.group();
            return parseProjectExperienceToJson(section);
        }
        return null;
    }

    /**
     * 解析教育经历为JSON
     */
    private String parseEducationToJson(String section) {
        List<String> items = new ArrayList<>();
        
        // 匹配学校名称（包含"大学"、"学院"等关键词）
        Pattern schoolPattern = Pattern.compile(
                "(\\d{4}[./-]\\d{1,2})?\\s*[-~至到]?\\s*(\\d{4}[./-]\\d{1,2})?\\s*" +
                "([\\u4e00-\\u9fa5]+(?:大学|学院|学校))\\s*" +
                "([\\u4e00-\\u9fa5]+(?:专业|工程|科学|技术|管理|设计)?)?\\s*" +
                "(博士|硕士|研究生|本科|大专|专科)?");
        
        Matcher matcher = schoolPattern.matcher(section);
        while (matcher.find()) {
            StringBuilder item = new StringBuilder("{");
            boolean hasContent = false;
            if (matcher.group(3) != null) {
                item.append("\"school\":\"").append(matcher.group(3)).append("\"");
                hasContent = true;
            }
            if (matcher.group(4) != null) {
                if (hasContent) item.append(",");
                item.append("\"major\":\"").append(matcher.group(4)).append("\"");
                hasContent = true;
            }
            if (matcher.group(5) != null) {
                if (hasContent) item.append(",");
                item.append("\"degree\":\"").append(matcher.group(5)).append("\"");
                hasContent = true;
            }
            if (matcher.group(1) != null) {
                if (hasContent) item.append(",");
                item.append("\"startDate\":\"").append(matcher.group(1)).append("\"");
                hasContent = true;
            }
            if (matcher.group(2) != null) {
                if (hasContent) item.append(",");
                item.append("\"endDate\":\"").append(matcher.group(2)).append("\"");
            }
            item.append("}");
            if (hasContent) {
                items.add(item.toString());
            }
        }
        
        return items.isEmpty() ? null : "[" + String.join(",", items) + "]";
    }

    /**
     * 解析工作经历为JSON
     */
    private String parseWorkExperienceToJson(String section) {
        List<String> items = new ArrayList<>();
        
        // 匹配公司名称和职位
        Pattern companyPattern = Pattern.compile(
                "(\\d{4}[./-]\\d{1,2})?\\s*[-~至到]?\\s*(\\d{4}[./-]\\d{1,2}|至今)?\\s*" +
                "([\\u4e00-\\u9fa5]+(?:公司|集团|科技|网络|信息|有限)[\\u4e00-\\u9fa5]*)\\s*" +
                "([\\u4e00-\\u9fa5]+(?:工程师|经理|主管|专员|开发|设计|运营)?)?");
        
        Matcher matcher = companyPattern.matcher(section);
        while (matcher.find()) {
            StringBuilder item = new StringBuilder("{");
            boolean hasContent = false;
            if (matcher.group(3) != null && matcher.group(3).length() > 2) {
                item.append("\"company\":\"").append(matcher.group(3)).append("\"");
                hasContent = true;
                if (matcher.group(4) != null) {
                    item.append(",\"position\":\"").append(matcher.group(4)).append("\"");
                }
                if (matcher.group(1) != null) {
                    item.append(",\"startDate\":\"").append(matcher.group(1)).append("\"");
                }
                if (matcher.group(2) != null) {
                    item.append(",\"endDate\":\"").append(matcher.group(2)).append("\"");
                }
                item.append("}");
                items.add(item.toString());
            }
        }
        
        return items.isEmpty() ? null : "[" + String.join(",", items) + "]";
    }

    /**
     * 解析项目经验为JSON
     */
    private String parseProjectExperienceToJson(String section) {
        List<String> items = new ArrayList<>();
        
        // 匹配项目名称
        Pattern projectPattern = Pattern.compile(
                "(\\d{4}[./-]\\d{1,2})?\\s*[-~至到]?\\s*(\\d{4}[./-]\\d{1,2}|至今)?\\s*" +
                "(?:项目名称[：:])?\\s*([\\u4e00-\\u9fa5a-zA-Z0-9]+(?:系统|平台|项目|APP|网站)[\\u4e00-\\u9fa5a-zA-Z0-9]*)");
        
        Matcher matcher = projectPattern.matcher(section);
        while (matcher.find()) {
            StringBuilder item = new StringBuilder("{");
            if (matcher.group(3) != null && matcher.group(3).length() > 2) {
                item.append("\"projectName\":\"").append(matcher.group(3)).append("\"");
                if (matcher.group(1) != null) {
                    item.append(",\"startDate\":\"").append(matcher.group(1)).append("\"");
                }
                if (matcher.group(2) != null) {
                    item.append(",\"endDate\":\"").append(matcher.group(2)).append("\"");
                }
                item.append("}");
                items.add(item.toString());
            }
        }
        
        return items.isEmpty() ? null : "[" + String.join(",", items) + "]";
    }

    /**
     * 清理文本
     */
    private String cleanText(String text) {
        if (text == null) {
            return null;
        }
        return text.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\\r\\n]+", " ");
    }
}