package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.dto.response.NotificationResponse;
import app.intelligent.resume.entity.Notification;
import app.intelligent.resume.repository.NotificationRepository;
import app.intelligent.resume.service.INotificationService;
import app.intelligent.resume.websocket.ChatWebSocketHandler;
import app.intelligent.resume.websocket.WebSocketMessage;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationRepository, Notification> 
        implements INotificationService {

    private final NotificationRepository notificationRepository;
    @Lazy
    private final ChatWebSocketHandler webSocketHandler;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String type, String title, String content,
                                           Long relatedId, String relatedType, String extraData) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setExtraData(extraData);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());
        
        notificationRepository.insert(notification);
        
        // 通过WebSocket推送通知
        try {
            NotificationResponse response = buildNotificationResponse(notification);
            webSocketHandler.sendToUser(userId, WebSocketMessage.newNotification(response));
        } catch (Exception e) {
            log.warn("WebSocket推送通知失败: {}", e.getMessage());
        }
        
        return notification;
    }

    @Override
    public Page<NotificationResponse> getUserNotifications(Long userId, String type, Boolean isRead, 
                                                            int page, int size) {
        Page<Notification> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead ? 1 : 0);
        }
        
        wrapper.orderByDesc(Notification::getCreateTime);
        
        Page<Notification> notificationPage = notificationRepository.selectPage(pageParam, wrapper);
        
        Page<NotificationResponse> responsePage = new Page<>(page, size, notificationPage.getTotal());
        responsePage.setRecords(notificationPage.getRecords().stream()
                .map(this::buildNotificationResponse)
                .collect(Collectors.toList()));
        
        return responsePage;
    }

    @Override
    public Map<String, Object> getUnreadCount(Long userId) {
        // 获取总未读数
        LambdaQueryWrapper<Notification> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Notification::getUserId, userId)
                   .eq(Notification::getIsRead, 0);
        long total = notificationRepository.selectCount(totalWrapper);
        
        // 按类型统计
        List<Map<String, Object>> typeCounts = notificationRepository.countUnreadByType(userId);
        Map<String, Long> byType = new HashMap<>();
        for (Map<String, Object> typeCount : typeCounts) {
            String type = (String) typeCount.get("type");
            Long count = ((Number) typeCount.get("count")).longValue();
            byType.put(type, count);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("byType", byType);
        
        return result;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此通知");
        }
        
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        notificationRepository.updateById(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId, String type) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        
        wrapper.set(Notification::getIsRead, 1)
               .set(Notification::getReadTime, LocalDateTime.now());
        
        notificationRepository.update(null, wrapper);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此通知");
        }
        
        notificationRepository.deleteById(notificationId);
    }


    // ========== 便捷方法实现 ==========

    @Override
    public void sendApplicationReceivedNotification(Long hrUserId, Long applicationId, 
                                                     String jobTitle, String seekerName) {
        String title = "收到新的简历投递";
        String content = String.format("%s 投递了您发布的岗位「%s」", seekerName, jobTitle);
        createNotification(hrUserId, "APPLICATION_RECEIVED", title, content, 
                applicationId, "APPLICATION", null);
    }

    @Override
    public void sendResumeViewedNotification(Long seekerUserId, Long resumeId, 
                                              String hrName, String companyName) {
        String title = "您的简历被查看";
        String content = String.format("%s（%s）查看了您的简历", hrName, companyName);
        createNotification(seekerUserId, "RESUME_VIEWED", title, content, 
                resumeId, "RESUME", null);
    }

    @Override
    public void sendApplicationStatusNotification(Long seekerUserId, Long applicationId, 
                                                   String jobTitle, Integer status) {
        String title;
        String content;
        String type;
        
        switch (status) {
            case 1:
                type = "APPLICATION_VIEWED";
                title = "简历已被查看";
                content = String.format("您投递的岗位「%s」，HR已查看您的简历", jobTitle);
                break;
            case 2:
                type = "APPLICATION_PASSED";
                title = "恭喜！通过简历筛选";
                content = String.format("您投递的岗位「%s」已通过简历筛选，请等待HR进一步联系", jobTitle);
                break;
            case 3:
                type = "APPLICATION_REJECTED";
                title = "投递结果通知";
                content = String.format("很遗憾，您投递的岗位「%s」暂时不太匹配，建议尝试其他岗位", jobTitle);
                break;
            case 4:
                type = "APPLICATION_OFFER";
                title = "恭喜！收到Offer";
                content = String.format("恭喜您！岗位「%s」已向您发出Offer", jobTitle);
                break;
            default:
                return;
        }
        
        createNotification(seekerUserId, type, title, content, applicationId, "APPLICATION", null);
    }

    @Override
    public void sendInterviewInviteNotification(Long seekerUserId, Long interviewId, 
                                                 String jobTitle, String interviewTime) {
        String title = "收到面试邀请";
        String content = String.format("您收到岗位「%s」的面试邀请，面试时间：%s", jobTitle, interviewTime);
        createNotification(seekerUserId, "INTERVIEW_INVITE", title, content, 
                interviewId, "INTERVIEW", null);
    }

    @Override
    public void sendNewMessageNotification(Long userId, Long conversationId, 
                                            String senderName, String content) {
        String title = "收到新消息";
        String notifyContent = String.format("%s: %s", senderName, 
                content.length() > 50 ? content.substring(0, 50) + "..." : content);
        createNotification(userId, "MESSAGE", title, notifyContent, 
                conversationId, "CONVERSATION", null);
    }

    /**
     * 构建通知响应
     */
    private NotificationResponse buildNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        BeanUtil.copyProperties(notification, response);
        response.setIsRead(notification.getIsRead() == 1);
        return response;
    }
}
