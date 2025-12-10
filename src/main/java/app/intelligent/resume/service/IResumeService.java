package app.intelligent.resume.service;

import app.intelligent.resume.entity.Resume;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 简历Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IResumeService extends IService<Resume> {

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
     * 创建简历
     */
    Resume createResume(Resume resume);

    /**
     * 更新简历
     */
    Resume updateResume(Long id, Resume resume);

    /**
     * 删除简历
     */
    boolean deleteResume(Long id);

    /**
     * 增加查看次数
     */
    void incrementViewCount(Long id);
}
