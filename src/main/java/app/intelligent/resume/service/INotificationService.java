package app.intelligent.resume.service;

import app.intelligent.resume.dto.response.NotificationResponse;
import app.intelligent.resume.entity.Notification;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 通知服务接口
 *
 * @author Intelligent Resume Team
 */
public interface INotificationService extends IService<Notification> {

    /**
     * 创建通知
     */
    Notification createNotification(Long userId, String type, String title, String content, 
                                    Long relatedId, String relatedType, String extraData);

    /**
     * 获取用户通知列表
     */
    Page<NotificationResponse> getUserNotifications(Long userId, String type, Boolean isRead, int page, int size);

    /**
     * 获取未读通知数量
     */
    Map<String, Object> getUnreadCount(Long userId);

    /**
     * 标记通知已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 全部标记已读
     */
    void markAllAsRead(Long userId, String type);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId, Long userId);

    // ========== 便捷方法 ==========

    /**
     * 发送投递通知（HR收到投递）
     */
    void sendApplicationReceivedNotification(Long hrUserId, Long applicationId, String jobTitle, String seekerName);

    /**
     * 发送简历被查看通知
     */
    void sendResumeViewedNotification(Long seekerUserId, Long resumeId, String hrName, String companyName);

    /**
     * 发送投递状态变更通知
     */
    void sendApplicationStatusNotification(Long seekerUserId, Long applicationId, String jobTitle, Integer status);

    /**
     * 发送面试邀请通知
     */
    void sendInterviewInviteNotification(Long seekerUserId, Long interviewId, String jobTitle, String interviewTime);

    /**
     * 发送新消息通知
     */
    void sendNewMessageNotification(Long userId, Long conversationId, String senderName, String content);
}
