package app.intelligent.resume.service;

import app.intelligent.resume.dto.request.ResumeDetailDTO;
import app.intelligent.resume.dto.request.ResumeOnlineCreateRequest;
import app.intelligent.resume.dto.request.ResumeSearchRequest;
import app.intelligent.resume.dto.request.ResumeUpdateRequest;
import app.intelligent.resume.dto.response.ResumeDetailResponse;
import app.intelligent.resume.dto.response.ResumeUploadResponse;
import app.intelligent.resume.entity.Resume;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 简历Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IResumeService extends IService<Resume> {

    /**
     * 上传简历文件（PDF/Word）
     * 对应接口：POST /api/resumes/upload
     *
     * @param file 简历文件
     * @return 上传结果
     */
    ResumeUploadResponse uploadResume(MultipartFile file);

    /**
     * 在线创建简历
     * 对应接口：POST /api/resumes
     *
     * @param request 创建请求
     * @return 创建的简历详情
     */
    ResumeDetailResponse createResumeOnline(ResumeOnlineCreateRequest request);

    /**
     * 获取当前用户的简历列表
     * 对应接口：GET /api/resumes/my
     *
     * @return 简历列表
     */
    List<Resume> listMyResumes();

    /**
     * 获取简历详情（包含详情和分析报告）
     * 对应接口：GET /api/resumes/{id}
     *
     * @param id 简历ID
     * @return 简历详情响应
     */
    ResumeDetailResponse getResumeDetail(Long id);

    /**
     * 更新简历
     * 对应接口：PUT /api/resumes/{id}
     *
     * @param id      简历ID
     * @param request 更新请求
     * @return 更新后的简历详情
     */
    ResumeDetailResponse updateResume(Long id, ResumeUpdateRequest request);

    /**
     * 删除简历（逻辑删除）
     * 对应接口：DELETE /api/resumes/{id}
     *
     * @param id 简历ID
     * @return 是否删除成功
     */
    boolean deleteResume(Long id);

    /**
     * 设置默认简历
     * 对应接口：PUT /api/resumes/{id}/default
     *
     * @param id 简历ID
     * @return 是否设置成功
     */
    boolean setDefaultResume(Long id);

    /**
     * 简历库搜索（HR专用）
     * 对应接口：GET /api/resumes/search
     *
     * @param request 搜索请求
     * @return 分页结果
     */
    Page<ResumeDetailResponse> searchResumes(ResumeSearchRequest request);

    /**
     * 下载简历
     * 对应接口：GET /api/resumes/{id}/download
     *
     * @param id 简历ID
     * @return 文件下载URL
     */
    String downloadResume(Long id);

    /**
     * 下载简历文件（流式下载）
     * 对应接口：GET /api/resumes/{id}/download
     *
     * @param id       简历ID
     * @param response HTTP响应对象
     */
    void downloadResumeFile(Long id, jakarta.servlet.http.HttpServletResponse response);

    // ========== 以下为原有方法，保持兼容 ==========

    /**
     * 根据用户ID查询简历列表
     */
    List<Resume> listByUserId(Long userId);

    /**
     * 查询用户的默认简历
     */
    Resume getDefaultByUserId(Long userId);

    /**
     * 查询所有简历列表
     */
    List<Resume> listAllResumes();

    /**
     * 分页查询简历
     */
    Page<Resume> pageResumes(int page, int size);

    /**
     * 创建简历（旧方法，保持兼容）
     */
    Resume createResume(Resume resume);

    /**
     * 更新简历（旧方法，保持兼容）
     */
    Resume updateResume(Long id, Resume resume);

    /**
     * 增加查看次数
     */
    void incrementViewCount(Long id);

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long id);

    /**
     * 检查用户是否有权限操作该简历
     *
     * @param resumeId 简历ID
     * @param userId   用户ID
     * @return 是否有权限
     */
    boolean hasPermission(Long resumeId, Long userId);
}
