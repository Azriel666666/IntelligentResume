package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.util.HtmlSanitizer;
import app.intelligent.resume.dto.response.ChatMessageResponse;
import app.intelligent.resume.entity.ChatMessage;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.repository.ChatMessageRepository;
import app.intelligent.resume.repository.UserRepository;
import app.intelligent.resume.service.IChatMessageService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageRepository, ChatMessage> 
        implements IChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessage message) {
        // HTML防注入处理
        if (message.getContent() != null) {
            message.setContent(HtmlSanitizer.sanitize(message.getContent()));
        }
        
        message.setCreateTime(LocalDateTime.now());
        message.setIsRead(0);
        message.setIsRecalled(0);
        
        chatMessageRepository.insert(message);
        
        return buildMessageResponse(message, message.getSenderId());
    }

    @Override
    public Page<ChatMessageResponse> getConversationMessages(Long conversationId, Long userId, 
                                                              int page, int size) {
        Page<ChatMessage> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByDesc(ChatMessage::getCreateTime);
        
        Page<ChatMessage> messagePage = chatMessageRepository.selectPage(pageParam, wrapper);
        
        Page<ChatMessageResponse> responsePage = new Page<>(page, size, messagePage.getTotal());
        List<ChatMessageResponse> records = messagePage.getRecords().stream()
                .map(m -> buildMessageResponse(m, userId))
                .collect(Collectors.toList());
        
        // 反转顺序，让最新消息在最后
        Collections.reverse(records);
        responsePage.setRecords(records);
        
        return responsePage;
    }

    @Override
    public Page<ChatMessageResponse> getMessagesBefore(Long conversationId, Long beforeId, 
                                                        Long userId, int limit) {
        List<ChatMessage> messages = chatMessageRepository.findBeforeId(conversationId, beforeId, limit);
        
        Page<ChatMessageResponse> responsePage = new Page<>(1, limit, messages.size());
        List<ChatMessageResponse> records = messages.stream()
                .map(m -> buildMessageResponse(m, userId))
                .collect(Collectors.toList());
        
        Collections.reverse(records);
        responsePage.setRecords(records);
        
        return responsePage;
    }

    @Override
    @Transactional
    public void markAllAsRead(Long conversationId, Long userId) {
        chatMessageRepository.markAllAsRead(conversationId, userId);
    }

    @Override
    @Transactional
    public void recallMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.selectById(messageId);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException("只能撤回自己发送的消息");
        }
        
        // 检查是否在2分钟内
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(message.getCreateTime(), now);
        if (minutes > 2) {
            throw new BusinessException("只能撤回2分钟内的消息");
        }
        
        message.setIsRecalled(1);
        message.setRecallTime(now);
        chatMessageRepository.updateById(message);
    }

    @Override
    public int countUnread(Long conversationId, Long userId) {
        return chatMessageRepository.countUnread(conversationId, userId);
    }


    /**
     * 构建消息响应
     */
    private ChatMessageResponse buildMessageResponse(ChatMessage message, Long currentUserId) {
        ChatMessageResponse response = new ChatMessageResponse();
        // 排除 attachments 字段，因为类型不同（String vs List）
        BeanUtil.copyProperties(message, response, "attachments");
        
        // 获取发送者信息
        User sender = userRepository.selectById(message.getSenderId());
        if (sender != null) {
            response.setSenderName(sender.getNickname() != null ? sender.getNickname() : sender.getUsername());
            response.setSenderAvatar(sender.getAvatar());
        }
        
        // 设置是否是自己发送的
        response.setIsSelf(message.getSenderId().equals(currentUserId));
        
        // 设置已读状态
        response.setIsRead(message.getIsRead() == 1);
        response.setIsRecalled(message.getIsRecalled() == 1);
        
        // 解析附件
        if (message.getAttachments() != null && !message.getAttachments().isEmpty()) {
            try {
                List<ChatMessageResponse.AttachmentInfo> attachments = objectMapper.readValue(
                        message.getAttachments(), 
                        new TypeReference<List<ChatMessageResponse.AttachmentInfo>>() {});
                response.setAttachments(attachments);
            } catch (Exception e) {
                log.warn("解析附件信息失败: {}", e.getMessage());
            }
        }
        
        return response;
    }
}
