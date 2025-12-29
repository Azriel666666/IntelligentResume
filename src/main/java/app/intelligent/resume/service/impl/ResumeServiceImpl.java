package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.dto.request.ResumeDetailDTO;
import app.intelligent.resume.dto.request.ResumeOnlineCreateRequest;
import app.intelligent.resume.dto.request.ResumeSearchRequest;
import app.intelligent.resume.dto.request.ResumeUpdateRequest;
import app.intelligent.resume.dto.response.ResumeDetailResponse;
import app.intelligent.resume.dto.response.ResumeUploadResponse;
import app.intelligent.resume.entity.AnalysisReport;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.ResumeRepository;
import app.intelligent.resume.security.SecurityUtils;
import app.intelligent.resume.service.IAnalysisReportService;
import app.intelligent.resume.service.IFileStorageService;
import app.intelligent.resume.service.IResumeDetailService;
import app.intelligent.resume.service.IResumeService;
import app.intelligent.resume.service.IUserService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简历Service实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeRepository, Resume> implements IResumeService {

    private final ResumeRepository resumeRepository;
    private final IResumeDetailService resumeDetailService;
    private final IFileStorageService fileStorageService;
    private final IUserService userService;
    private final IAnalysisReportService analysisReportService;
    private final ObjectMapper objectMapper;

    /**
     * 支持的简历文件类型
     */
    private static final List<String> SUPPORTED_FILE_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * 最大文件大小：10MB
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 每个用户最大简历数量
     */
    private static final int MAX_RESUME_COUNT = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeUploadResponse uploadResume(MultipartFile file) {
        // 1. 验证文件
        validateFile(file);

        // 2. 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        // 3. 检查简历数量限制
        long resumeCount = count(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, currentUser.getId())
                .eq(Resume::getDeleted, 0));
        if (resumeCount >= MAX_RESUME_COUNT) {
            throw new BusinessException(ResultCode.RESUME_COUNT_EXCEEDED);
        }

        // 4. 上传文件到存储服务
        String fileUrl = fileStorageService.uploadFile(file, "resume", "resume");

        // 5. 获取文件信息
        String originalFilename = file.getOriginalFilename();
        String fileType = getFileExtension(originalFilename);
        long fileSize = file.getSize();

        // 6. 创建简历记录
        Resume resume = new Resume();
        resume.setUserId(currentUser.getId());
        resume.setTitle(generateResumeTitle(originalFilename));
        resume.setFileUrl(fileUrl);
        resume.setFileName(originalFilename);
        resume.setFileType(fileType);
        resume.setFileSize(fileSize);
        resume.setParseStatus(1); // 解析中
        resume.setStatus(1); // 正常
        resume.setIsDefault(resumeCount == 0 ? 1 : 0); // 第一份简历设为默认
        resume.setViewCount(0);
        resume.setDownloadCount(0);

        save(resume);

        log.info("简历上传成功, userId={}, resumeId={}, fileName={}", 
                currentUser.getId(), resume.getId(), originalFilename);

        // 7. TODO: 异步调用文档解析服务（后续实现）
        // parseResumeAsync(resume.getId());

        return ResumeUploadResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .fileUrl(fileUrl)
                .fileName(originalFilename)
                .fileType(fileType)
                .fileSize(fileSize)
                .parseStatus(resume.getParseStatus())
                .message("简历上传成功，正在解析中")
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeDetailResponse createResumeOnline(ResumeOnlineCreateRequest request) {
        // 1. 获取当前用户
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }

        // 2. 检查简历数量限制
        long resumeCount = count(new LambdaQueryWrapper<Resume>()
                .eq(Resume::getUserId, currentUser.getId())
                .eq(Resume::getDeleted, 0));
        if (resumeCount >= MAX_RESUME_COUNT) {
            throw new BusinessException(ResultCode.RESUME_COUNT_EXCEEDED);
        }

        // 3. 创建简历主记录
        Resume resume = new Resume();
        resume.setUserId(currentUser.getId());
        resume.setTitle(request.getTitle());
        resume.setParseStatus(2); // 在线创建的简历直接标记为解析成功
        resume.setStatus(1); // 正常
        resume.setIsDefault(resumeCount == 0 ? 1 : 0);
        resume.setViewCount(0);
        resume.setDownloadCount(0);

        save(resume);

        // 4. 创建简历详情
        if (request.getDetail() != null) {
            ResumeDetail detail = convertToResumeDetail(request.getDetail());
            detail.setResumeId(resume.getId());
            resumeDetailService.saveOrUpdateDetail(detail);
        }

        log.info("在线创建简历成功, userId={}, resumeId={}", currentUser.getId(), resume.getId());

        return getResumeDetail(resume.getId());
    }

    @Override
    public List<Resume> listMyResumes() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        return listByUserId(currentUser.getId());
    }

    @Override
    public ResumeDetailResponse getResumeDetail(Long id) {
        // 1. 获取简历基本信息
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 2. 权限检查
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            // 求职者只能查看自己的简历
            if (SecurityUtils.isSeeker() && !resume.getUserId().equals(currentUser.getId())) {
                throw new BusinessException(ResultCode.RESUME_NO_PERMISSION);
            }
            // HR查看简历时记录日志（后续可扩展）
            if (SecurityUtils.isHR()) {
                log.info("HR查看简历, hrId={}, resumeId={}", currentUser.getId(), id);
            }
        }

        // 3. 构建响应
        ResumeDetailResponse response = new ResumeDetailResponse();
        BeanUtil.copyProperties(resume, response);

        // 4. 获取简历详情
        ResumeDetail detail = resumeDetailService.getByResumeId(id);
        if (detail != null) {
            response.setDetail(convertToResumeDetailDTO(detail));
        }

        // 5. 获取分析报告
        AnalysisReport report = analysisReportService.getLatestByResumeId(id);
        response.setAnalysisReport(report);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeDetailResponse updateResume(Long id, ResumeUpdateRequest request) {
        // 1. 获取简历
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 2. 权限检查
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.RESUME_NO_PERMISSION);
        }

        // 3. 更新简历基本信息
        if (StringUtils.hasText(request.getTitle())) {
            resume.setTitle(request.getTitle());
        }
        if (request.getStatus() != null) {
            resume.setStatus(request.getStatus());
        }
        updateById(resume);

        // 4. 更新简历详情
        if (request.getDetail() != null) {
            ResumeDetail detail = convertToResumeDetail(request.getDetail());
            detail.setResumeId(id);
            resumeDetailService.saveOrUpdateDetail(detail);
        }

        log.info("更新简历成功, resumeId={}", id);

        return getResumeDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResume(Long id) {
        // 1. 获取简历
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 2. 权限检查
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (!SecurityUtils.isAdmin() && !resume.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.RESUME_NO_PERMISSION);
        }

        // 3. 逻辑删除
        boolean result = removeById(id);

        // 4. 如果删除的是默认简历，需要重新设置默认简历
        if (result && resume.getIsDefault() != null && resume.getIsDefault() == 1) {
            List<Resume> remainingResumes = listByUserId(resume.getUserId());
            if (!remainingResumes.isEmpty()) {
                Resume newDefault = remainingResumes.get(0);
                newDefault.setIsDefault(1);
                updateById(newDefault);
            }
        }

        log.info("删除简历成功, resumeId={}", id);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultResume(Long id) {
        // 1. 获取简历
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 2. 权限检查
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCode.USER_NOT_LOGIN);
        }
        if (!resume.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCode.RESUME_NO_PERMISSION);
        }

        // 3. 取消原来的默认简历
        Resume oldDefault = getDefaultByUserId(currentUser.getId());
        if (oldDefault != null && !oldDefault.getId().equals(id)) {
            oldDefault.setIsDefault(0);
            updateById(oldDefault);
        }

        // 4. 设置新的默认简历
        resume.setIsDefault(1);
        updateById(resume);

        log.info("设置默认简历成功, userId={}, resumeId={}", currentUser.getId(), id);
        return true;
    }

    @Override
    public Page<ResumeDetailResponse> searchResumes(ResumeSearchRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Resume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Resume::getDeleted, 0);
        queryWrapper.eq(Resume::getStatus, 1); // 只搜索正常状态的简历

        // 分页查询简历
        Page<Resume> page = new Page<>(request.getPage(), request.getSize());
        Page<Resume> resumePage = page(page, queryWrapper);

        // 转换为详情响应
        Page<ResumeDetailResponse> responsePage = new Page<>();
        responsePage.setTotal(resumePage.getTotal());
        responsePage.setCurrent(resumePage.getCurrent());
        responsePage.setSize(resumePage.getSize());
        responsePage.setPages(resumePage.getPages());

        List<ResumeDetailResponse> records = resumePage.getRecords().stream()
                .map(resume -> {
                    ResumeDetailResponse response = new ResumeDetailResponse();
                    BeanUtil.copyProperties(resume, response);

                    // 获取简历详情
                    ResumeDetail detail = resumeDetailService.getByResumeId(resume.getId());
                    if (detail != null) {
                        // 根据搜索条件过滤
                        if (!matchSearchCriteria(detail, request)) {
                            return null;
                        }
                        response.setDetail(convertToResumeDetailDTO(detail));
                    }
                    return response;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());

        responsePage.setRecords(records);
        return responsePage;
    }

    @Override
    public String downloadResume(Long id) {
        // 1. 获取简历
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 2. 检查文件是否存在
        if (!StringUtils.hasText(resume.getFileUrl())) {
            throw new BusinessException(ResultCode.RESUME_FILE_NOT_EXIST);
        }

        // 3. 增加下载次数
        incrementDownloadCount(id);

        // 4. 返回文件URL（可以返回预签名URL以增加安全性）
        log.info("下载简历, resumeId={}", id);
        return resume.getFileUrl();
    }

    // ========== 原有方法实现 ==========

    @Override
    public List<Resume> listByUserId(Long userId) {
        return resumeRepository.selectByUserId(userId);
    }

    @Override
    public Resume getDefaultByUserId(Long userId) {
        return resumeRepository.selectDefaultByUserId(userId);
    }

    @Override
    public List<Resume> listAllResumes() {
        return list();
    }

    @Override
    public Page<Resume> pageResumes(int page, int size) {
        return page(new Page<>(page, size));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Resume createResume(Resume resume) {
        // 设置默认值
        if (resume.getParseStatus() == null) {
            resume.setParseStatus(0); // 未解析
        }
        if (resume.getStatus() == null) {
            resume.setStatus(1); // 正常
        }
        if (resume.getViewCount() == null) {
            resume.setViewCount(0);
        }
        if (resume.getDownloadCount() == null) {
            resume.setDownloadCount(0);
        }

        save(resume);
        return resume;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Resume updateResume(Long id, Resume resume) {
        Resume existingResume = getById(id);
        if (existingResume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }

        // 更新字段
        if (resume.getTitle() != null) {
            existingResume.setTitle(resume.getTitle());
        }
        if (resume.getFileUrl() != null) {
            existingResume.setFileUrl(resume.getFileUrl());
        }
        if (resume.getFileName() != null) {
            existingResume.setFileName(resume.getFileName());
        }
        if (resume.getFileType() != null) {
            existingResume.setFileType(resume.getFileType());
        }
        if (resume.getFileSize() != null) {
            existingResume.setFileSize(resume.getFileSize());
        }
        if (resume.getStatus() != null) {
            existingResume.setStatus(resume.getStatus());
        }

        updateById(existingResume);
        return existingResume;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        Resume resume = getById(id);
        if (resume != null) {
            resume.setViewCount(resume.getViewCount() + 1);
            updateById(resume);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementDownloadCount(Long id) {
        Resume resume = getById(id);
        if (resume != null) {
            resume.setDownloadCount(resume.getDownloadCount() + 1);
            updateById(resume);
        }
    }

    @Override
    public boolean hasPermission(Long resumeId, Long userId) {
        Resume resume = getById(resumeId);
        if (resume == null) {
            return false;
        }
        return resume.getUserId().equals(userId);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 验证上传的文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_EMPTY);
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEEDED);
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_FILE_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.FILE_TYPE_NOT_SUPPORT);
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 生成简历标题
     */
    private String generateResumeTitle(String filename) {
        if (filename == null) {
            return "我的简历";
        }
        // 去掉扩展名
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex);
        }
        return filename;
    }

    /**
     * 将ResumeDetailDTO转换为ResumeDetail实体
     */
    private ResumeDetail convertToResumeDetail(ResumeDetailDTO dto) {
        ResumeDetail detail = new ResumeDetail();
        BeanUtil.copyProperties(dto, detail, "educationList", "workExperienceList", 
                "projectExperienceList", "skills", "certificates");

        try {
            // 转换列表字段为JSON
            if (dto.getEducationList() != null) {
                detail.setEducationJson(objectMapper.writeValueAsString(dto.getEducationList()));
            }
            if (dto.getWorkExperienceList() != null) {
                detail.setWorkExperienceJson(objectMapper.writeValueAsString(dto.getWorkExperienceList()));
            }
            if (dto.getProjectExperienceList() != null) {
                detail.setProjectExperienceJson(objectMapper.writeValueAsString(dto.getProjectExperienceList()));
            }
            if (dto.getSkills() != null) {
                detail.setSkillsJson(objectMapper.writeValueAsString(dto.getSkills()));
                detail.setSkillTags(String.join(",", dto.getSkills()));
            }
            if (dto.getCertificates() != null) {
                detail.setCertificatesJson(objectMapper.writeValueAsString(dto.getCertificates()));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON转换失败", e);
        }

        return detail;
    }

    /**
     * 将ResumeDetail实体转换为ResumeDetailDTO
     */
    private ResumeDetailDTO convertToResumeDetailDTO(ResumeDetail detail) {
        ResumeDetailDTO dto = new ResumeDetailDTO();
        BeanUtil.copyProperties(detail, dto, "educationJson", "workExperienceJson", 
                "projectExperienceJson", "skillsJson", "certificatesJson");

        try {
            // 转换JSON字段为列表
            if (StringUtils.hasText(detail.getEducationJson())) {
                dto.setEducationList(objectMapper.readValue(detail.getEducationJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeDetailDTO.EducationDTO.class)));
            }
            if (StringUtils.hasText(detail.getWorkExperienceJson())) {
                dto.setWorkExperienceList(objectMapper.readValue(detail.getWorkExperienceJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeDetailDTO.WorkExperienceDTO.class)));
            }
            if (StringUtils.hasText(detail.getProjectExperienceJson())) {
                dto.setProjectExperienceList(objectMapper.readValue(detail.getProjectExperienceJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ResumeDetailDTO.ProjectExperienceDTO.class)));
            }
            if (StringUtils.hasText(detail.getSkillsJson())) {
                dto.setSkills(objectMapper.readValue(detail.getSkillsJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
            if (StringUtils.hasText(detail.getCertificatesJson())) {
                dto.setCertificates(objectMapper.readValue(detail.getCertificatesJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)));
            }
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败", e);
        }

        return dto;
    }

    /**
     * 检查简历详情是否匹配搜索条件
     */
    private boolean matchSearchCriteria(ResumeDetail detail, ResumeSearchRequest request) {
        // 关键词匹配（姓名、技能、职位）
        if (StringUtils.hasText(request.getKeyword())) {
            String keyword = request.getKeyword().toLowerCase();
            boolean matched = false;
            if (detail.getName() != null && detail.getName().toLowerCase().contains(keyword)) {
                matched = true;
            }
            if (detail.getSkillTags() != null && detail.getSkillTags().toLowerCase().contains(keyword)) {
                matched = true;
            }
            if (detail.getExpectedPosition() != null && detail.getExpectedPosition().toLowerCase().contains(keyword)) {
                matched = true;
            }
            if (!matched) {
                return false;
            }
        }

        // 学历匹配
        if (StringUtils.hasText(request.getEducation())) {
            if (detail.getHighestEducation() == null || 
                    !detail.getHighestEducation().contains(request.getEducation())) {
                return false;
            }
        }

        // 工作年限匹配
        if (request.getWorkYearsMin() != null && detail.getWorkYears() != null) {
            if (detail.getWorkYears() < request.getWorkYearsMin()) {
                return false;
            }
        }
        if (request.getWorkYearsMax() != null && detail.getWorkYears() != null) {
            if (detail.getWorkYears() > request.getWorkYearsMax()) {
                return false;
            }
        }

        // 城市匹配
        if (StringUtils.hasText(request.getCity())) {
            if (detail.getCurrentCity() == null || 
                    !detail.getCurrentCity().contains(request.getCity())) {
                return false;
            }
        }

        // 技能标签匹配
        if (StringUtils.hasText(request.getSkillTags())) {
            if (detail.getSkillTags() == null) {
                return false;
            }
            String[] requiredSkills = request.getSkillTags().split(",");
            String detailSkills = detail.getSkillTags().toLowerCase();
            for (String skill : requiredSkills) {
                if (!detailSkills.contains(skill.trim().toLowerCase())) {
                    return false;
                }
            }
        }

        return true;
    }
}
