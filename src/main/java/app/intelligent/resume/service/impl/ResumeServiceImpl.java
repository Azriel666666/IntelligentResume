package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.result.ResultCode;
import app.intelligent.resume.entity.Resume;
import app.intelligent.resume.repository.ResumeRepository;
import app.intelligent.resume.service.IResumeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 简历Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl extends ServiceImpl<ResumeRepository, Resume> implements IResumeService {

    private final ResumeRepository resumeRepository;

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
    public boolean deleteResume(Long id) {
        Resume resume = getById(id);
        if (resume == null) {
            throw new BusinessException(ResultCode.RESUME_NOT_EXIST);
        }
        return removeById(id);
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
}
