package app.intelligent.resume.service;

import app.intelligent.resume.entity.MatchRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 匹配记录Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IMatchRecordService extends IService<MatchRecord> {

    /**
     * 根据简历ID查询匹配记录
     */
    List<MatchRecord> listByResumeId(Long resumeId);

    /**
     * 根据岗位ID查询匹配记录
     */
    List<MatchRecord> listByJobId(Long jobId);

    /**
     * 创建匹配记录
     */
    MatchRecord createMatch(MatchRecord matchRecord);

    /**
     * 查询简历和岗位的匹配记录
     */
    MatchRecord getByResumeAndJob(Long resumeId, Long jobId);
}
