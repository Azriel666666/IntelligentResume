package app.intelligent.resume.service.impl;

import app.intelligent.resume.common.exception.BusinessException;
import app.intelligent.resume.common.util.HtmlSanitizer;
import app.intelligent.resume.dto.request.ConversationCreateRequest;
import app.intelligent.resume.dto.response.ConversationResponse;
import app.intelligent.resume.entity.*;
import app.intelligent.resume.repository.ConversationRepository;
import app.intelligent.resume.repository.JobRepository;
import app.intelligent.resume.repository.UserRepository;
import app.intelligent.resume.service.IChatMessageService;
import app.intelligent.resume.service.IConversationService;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话服务实现类
 *
 * @author Intelligent Resume Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationRepository, Conversation> 
        implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    @Lazy
    private final IChatMessageService chatMessageService;

    @Override
    @Transactional
    public ConversationResponse createOrGetConversation(Long currentUserId, Integer userType, 
                                                         ConversationCreateRequest request) {
        Long hrUserId;
        Long seekerUserId;
        
        // 根据用户类型确定HR和求职者
        if (userType == 2) { // HR
            hrUserId = currentUserId;
            seekerUserId = request.getTargetUserId();
        } else if (userType == 3) { // 求职者
            seekerUserId = currentUserId;
            hrUserId = request.getTargetUserId();
        } else {
            throw new BusinessException("无效的用户类型");
        }

        // 验证目标用户存在
        User targetUser = userRepository.selectById(request.getTargetUserId());
        if (targetUser == null) {
            throw new BusinessException("目标用户不存在");
        }

        // 验证岗位存在
        Job job = null;
        if (request.getJobId() != null) {
            job = jobRepository.selectById(request.getJobId());
            if (job == null) {
                throw new BusinessException("岗位不存在");
            }
        }

        // 查找是否已存在会话
        Conversation existing = conversationRepository.findByHrAndSeekerAndJob(
                hrUserId, seekerUserId, request.getJobId());
        
        if (existing != null) {
            // 如果会话已关闭，重新打开
            if (existing.getStatus() != 1) {
                existing.setStatus(1);
                existing.setUpdateTime(LocalDateTime.now());
                conversationRepository.updateById(existing);
            }
            return buildConversationResponse(existing, currentUserId, userType);
        }

        // 创建新会话
        Conversation conversation = new Conversation();
        conversation.setHrUserId(hrUserId);
        conversation.setSeekerUserId(seekerUserId);
        conversation.setJobId(request.getJobId());
        conversation.setApplicationId(request.getApplicationId());
        conversation.setHrUnreadCount(0);
        conversation.setSeekerUnreadCount(0);
        conversation.setStatus(1);
        conversation.setCreateTime(LocalDateTime.now());
        conversation.setUpdateTime(LocalDateTime.now());
        
        conversationRepository.insert(conversation);

        // 如果有初始消息，发送第一条消息
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            String sanitizedMessage = HtmlSanitizer.sanitize(request.getInitialMessage());
            
            ChatMessage message = new ChatMessage();
            message.setConversationId(conversation.getId());
            message.setSenderId(currentUserId);
            message.setReceiverId(request.getTargetUserId());
            message.setMessageType("TEXT");
            message.setContent(sanitizedMessage);
            message.setIsRead(0);
            message.setIsRecalled(0);
            message.setCreateTime(LocalDateTime.now());
            
            chatMessageService.sendMessage(message);
            
            // 更新会话最后消息
            conversation.setLastMessageContent(sanitizedMessage.length() > 100 
                    ? sanitizedMessage.substring(0, 100) + "..." : sanitizedMessage);
            conversation.setLastMessageTime(LocalDateTime.now());
            
            // 更新对方未读数
            if (userType == 2) {
                conversation.setSeekerUnreadCount(1);
            } else {
                conversation.setHrUnreadCount(1);
            }
            conversationRepository.updateById(conversation);
        }

        return buildConversationResponse(conversation, currentUserId, userType);
    }

    @Override
    public Page<ConversationResponse> getUserConversations(Long userId, Integer userType, 
                                                            Integer status, int page, int size) {
        Page<Conversation> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        
        if (userType == 2) { // HR
            wrapper.eq(Conversation::getHrUserId, userId);
        } else { // 求职者
            wrapper.eq(Conversation::getSeekerUserId, userId);
        }
        
        if (status != null && status != 0) {
            wrapper.eq(Conversation::getStatus, status);
        }
        
        wrapper.orderByDesc(Conversation::getLastMessageTime);
        
        Page<Conversation> conversationPage = conversationRepository.selectPage(pageParam, wrapper);
        
        Page<ConversationResponse> responsePage = new Page<>(page, size, conversationPage.getTotal());
        responsePage.setRecords(conversationPage.getRecords().stream()
                .map(c -> buildConversationResponse(c, userId, userType))
                .collect(Collectors.toList()));
        
        return responsePage;
    }

    @Override
    public ConversationResponse getConversationDetail(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!isParticipant(conversationId, userId)) {
            throw new BusinessException("无权限查看此会话");
        }
        
        Integer userType = userId.equals(conversation.getHrUserId()) ? 2 : 3;
        return buildConversationResponse(conversation, userId, userType);
    }

    @Override
    @Transactional
    public void closeConversation(Long conversationId, Long hrUserId) {
        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        
        if (!conversation.getHrUserId().equals(hrUserId)) {
            throw new BusinessException("只有HR可以关闭会话");
        }
        
        conversation.setStatus(2); // HR关闭
        conversation.setUpdateTime(LocalDateTime.now());
        conversationRepository.updateById(conversation);
    }

    @Override
    public void updateLastMessage(Long conversationId, Long messageId, String content) {
        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(messageId);
            conversation.setLastMessageContent(content);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUpdateTime(LocalDateTime.now());
            conversationRepository.updateById(conversation);
        }
    }

    @Override
    public void incrementHrUnreadCount(Long conversationId) {
        conversationRepository.incrementHrUnreadCount(conversationId);
    }

    @Override
    public void incrementSeekerUnreadCount(Long conversationId) {
        conversationRepository.incrementSeekerUnreadCount(conversationId);
    }

    @Override
    public void clearHrUnreadCount(Long conversationId) {
        conversationRepository.clearHrUnreadCount(conversationId);
    }

    @Override
    public void clearSeekerUnreadCount(Long conversationId) {
        conversationRepository.clearSeekerUnreadCount(conversationId);
    }

    @Override
    public boolean isParticipant(Long conversationId, Long userId) {
        Conversation conversation = conversationRepository.selectById(conversationId);
        if (conversation == null) {
            return false;
        }
        return userId.equals(conversation.getHrUserId()) || userId.equals(conversation.getSeekerUserId());
    }

    /**
     * 构建会话响应
     */
    private ConversationResponse buildConversationResponse(Conversation conversation, Long currentUserId, Integer userType) {
        ConversationResponse response = new ConversationResponse();
        BeanUtil.copyProperties(conversation, response);
        
        // 获取对方用户信息
        Long otherUserId = userType == 2 ? conversation.getSeekerUserId() : conversation.getHrUserId();
        User otherUser = userRepository.selectById(otherUserId);
        if (otherUser != null) {
            response.setOtherUserId(otherUserId);
            response.setOtherUserName(otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername());
            response.setOtherUserAvatar(otherUser.getAvatar());
            if (userType == 3 && otherUser.getCompanyName() != null) {
                response.setCompanyName(otherUser.getCompanyName());
            }
        }
        
        // 获取岗位信息
        if (conversation.getJobId() != null) {
            Job job = jobRepository.selectById(conversation.getJobId());
            if (job != null) {
                response.setJobTitle(job.getJobTitle());
            }
        }
        
        // 设置当前用户的未读数
        if (userType == 2) {
            response.setUnreadCount(conversation.getHrUnreadCount());
        } else {
            response.setUnreadCount(conversation.getSeekerUnreadCount());
        }
        
        return response;
    }
}
