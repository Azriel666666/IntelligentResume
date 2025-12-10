package app.intelligent.resume.service;

import app.intelligent.resume.entity.ResumeDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 简历详情Service接口
 *
 * @author Intelligent Resume Team
 */
public interface IResumeDetailService extends IService<ResumeDetail> {

    /**
     * 根据简历ID查询详情
     */
    ResumeDetail getByResumeId(Long resumeId);

    /**
     * 创建或更新简历详情
     */
    ResumeDetail saveOrUpdateDetail(ResumeDetail detail);
}
