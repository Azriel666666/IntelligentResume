package app.intelligent.resume.service.impl;

import app.intelligent.resume.entity.ResumeDetail;
import app.intelligent.resume.repository.ResumeDetailRepository;
import app.intelligent.resume.service.IResumeDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 简历详情Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class ResumeDetailServiceImpl extends ServiceImpl<ResumeDetailRepository, ResumeDetail> implements IResumeDetailService {

    private final ResumeDetailRepository resumeDetailRepository;

    @Override
    public ResumeDetail getByResumeId(Long resumeId) {
        return resumeDetailRepository.selectByResumeId(resumeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeDetail saveOrUpdateDetail(ResumeDetail detail) {
        ResumeDetail existing = getByResumeId(detail.getResumeId());
        if (existing != null) {
            detail.setId(existing.getId());
            updateById(detail);
        } else {
            save(detail);
        }
        return detail;
    }
}
