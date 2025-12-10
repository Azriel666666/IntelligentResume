package app.intelligent.resume.service.impl;

import app.intelligent.resume.entity.MatchRecord;
import app.intelligent.resume.repository.MatchRecordRepository;
import app.intelligent.resume.service.IMatchRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 匹配记录Service实现类
 *
 * @author Intelligent Resume Team
 */
@Service
@RequiredArgsConstructor
public class MatchRecordServiceImpl extends ServiceImpl<MatchRecordRepository, MatchRecord> implements IMatchRecordService {

    private final MatchRecordRepository matchRecordRepository;

    @Override
    public List<MatchRecord> listByResumeId(Long resumeId) {
        return matchRecordRepository.selectByResumeId(resumeId);
    }

    @Override
    public List<MatchRecord> listByJobId(Long jobId) {
        return matchRecordRepository.selectByJobId(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchRecord createMatch(MatchRecord matchRecord) {
        // 检查是否已存在匹配记录
        MatchRecord existing = getByResumeAndJob(matchRecord.getResumeId(), matchRecord.getJobId());
        if (existing != null) {
            // 更新现有记录
            matchRecord.setId(existing.getId());
            updateById(matchRecord);
            return matchRecord;
        }

        // 设置默认值
        if (matchRecord.getMatchType() == null) {
            matchRecord.setMatchType(1); // 系统推荐
        }

        save(matchRecord);
        return matchRecord;
    }

    @Override
    public MatchRecord getByResumeAndJob(Long resumeId, Long jobId) {
        return matchRecordRepository.selectByResumeAndJob(resumeId, jobId);
    }
}
