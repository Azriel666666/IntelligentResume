package app.intelligent.resume.service;

import app.intelligent.resume.entity.ResumeDetail;

import java.io.InputStream;

/**
 * 简历解析服务接口
 * 对应需求文档4.4简历解析模块
 *
 * @author Intelligent Resume Team
 */
public interface IResumeParserService {

    /**
     * 解析简历文件
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 解析后的简历详情
     */
    ResumeDetail parseResume(InputStream inputStream, String fileName);

    /**
     * 从URL解析简历
     *
     * @param fileUrl  文件URL
     * @param fileName 文件名
     * @return 解析后的简历详情
     */
    ResumeDetail parseResumeFromUrl(String fileUrl, String fileName);

    /**
     * 提取简历文本内容
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @return 文本内容
     */
    String extractText(InputStream inputStream, String fileName);
}