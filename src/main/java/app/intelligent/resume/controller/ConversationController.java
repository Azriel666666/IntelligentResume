package app.intelligent.resume.controller;

import app.intelligent.resume.common.result.Result;
import app.intelligent.resume.common.util.HtmlSanitizer;
import app.intelligent.resume.dto.request.ChatMessageRequest;
import app.intelligent.resume.dto.request.ConversationCreateRequest;
import app.intelligent.resume.dto.response.ChatMessageResponse;
import app.intelligent.resume.dto.response.ConversationResponse;
import app.intelligent.resume.entity.ChatMessage;
import app.intelligent.resume.entity.Conversation;
import app.intelligent.resume.entity.User;
import app.intelligent.resume.service.IChatMessageService;
import app.intelligent.resume.service.IConversationService;
import app.intelligent.resume.service.IUserService;
import app.intelligent.resume.websocket.ChatWebSocketHandler;
import app.intelligent.resume.websocket.WebSocketMessage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 会话控制器
 * 对应需求文档4.10消息通讯模块
 *
 * @author Intelligent Resume Team
 */
@Tag(name = "会话管理", description = "会话和消息相关接口")
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Validated
public class ConversationController {

    private final IConversationService conversationService;
    private final IChatMessageService chatMessageService;
    private final IUserService userService;
    private final ChatWebSocketHandler webSocketHandler;

    /**
     * 获取当前用户
     */
    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    /**
     * 4.10.1 创建/获取会话
     * POST /api/conversations
     */
    @Operation(summary = "创建或获取会话", description = "HR或求职者主动发起会话")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @PostMapping
    public Result<ConversationResponse> createConversation(@Valid @RequestBody ConversationCreateRequest request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        ConversationResponse response = conversationService.createOrGetConversation(
                currentUser.getId(), currentUser.getUserType(), request);
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.10.1 获取会话列表
     * GET /api/conversations
     */
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有会话")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @GetMapping
    public Result<ConversationResponse> listConversations(
            @Parameter(description = "会话状态：0-全部 1-正常 2-已关闭") @RequestParam(defaultValue = "0") Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        Page<ConversationResponse> conversationPage = conversationService.getUserConversations(
                currentUser.getId(), currentUser.getUserType(), status, page, size);
        
        return Result.success(conversationPage.getRecords(), conversationPage.getTotal(), 
                conversationPage.getPages(), conversationPage.getCurrent(), conversationPage.getSize());
    }

    /**
     * 4.10.1 获取会话详情
     * GET /api/conversations/{id}
     */
    @Operation(summary = "获取会话详情", description = "获取会话详情及消息历史")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @GetMapping("/{id}")
    public Result<ConversationResponse> getConversation(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        ConversationResponse response = conversationService.getConversationDetail(id, currentUser.getId());
        return Result.success(Collections.singletonList(response));
    }

    /**
     * 4.10.1 关闭会话
     * PUT /api/conversations/{id}/close
     */
    @Operation(summary = "关闭会话", description = "HR关闭会话")
    @PreAuthorize("hasAnyRole('HR', 'ADMIN')")
    @PutMapping("/{id}/close")
    public Result<Void> closeConversation(
            @Parameter(description = "会话ID") @PathVariable Long id) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        conversationService.closeConversation(id, currentUser.getId());
        return Result.success("会话已关闭");
    }


    /**
     * 4.10.2 发送消息
     * POST /api/conversations/{conversationId}/messages
     */
    @Operation(summary = "发送消息", description = "在会话中发送消息")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @PostMapping("/{conversationId}/messages")
    public Result<ChatMessageResponse> sendMessage(
            @Parameter(description = "会话ID") @PathVariable Long conversationId,
            @Valid @RequestBody ChatMessageRequest request) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        // 验证会话权限
        if (!conversationService.isParticipant(conversationId, currentUser.getId())) {
            return Result.failed("无权限发送消息到此会话");
        }
        
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.failed("会话不存在");
        }
        
        if (conversation.getStatus() != 1) {
            return Result.failed("会话已关闭，无法发送消息");
        }
        
        // 确定接收者
        Long receiverId = currentUser.getId().equals(conversation.getHrUserId()) 
                ? conversation.getSeekerUserId() 
                : conversation.getHrUserId();
        
        // HTML防注入处理
        String sanitizedContent = HtmlSanitizer.sanitize(request.getContent());
        
        // 创建消息
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(currentUser.getId());
        message.setReceiverId(receiverId);
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : "TEXT");
        message.setContent(sanitizedContent);
        message.setAttachments(request.getAttachments());
        message.setIsRead(0);
        message.setIsRecalled(0);
        message.setCreateTime(LocalDateTime.now());
        
        ChatMessageResponse savedMessage = chatMessageService.sendMessage(message);
        
        // 更新会话最后消息
        String preview = sanitizedContent.length() > 100 
                ? sanitizedContent.substring(0, 100) + "..." : sanitizedContent;
        conversationService.updateLastMessage(conversationId, savedMessage.getId(), preview);
        
        // 更新未读数
        if (currentUser.getId().equals(conversation.getHrUserId())) {
            conversationService.incrementSeekerUnreadCount(conversationId);
        } else {
            conversationService.incrementHrUnreadCount(conversationId);
        }
        
        // 通过WebSocket推送给接收者
        webSocketHandler.sendToUser(receiverId, WebSocketMessage.newMessage(savedMessage));
        
        return Result.success(Collections.singletonList(savedMessage));
    }

    /**
     * 4.10.2 获取消息历史
     * GET /api/conversations/{conversationId}/messages
     */
    @Operation(summary = "获取消息历史", description = "获取会话的消息列表")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @GetMapping("/{conversationId}/messages")
    public Result<ChatMessageResponse> getMessages(
            @Parameter(description = "会话ID") @PathVariable Long conversationId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "获取此ID之前的消息") @RequestParam(required = false) Long beforeId) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        // 验证会话权限
        if (!conversationService.isParticipant(conversationId, currentUser.getId())) {
            return Result.failed("无权限查看此会话");
        }
        
        Page<ChatMessageResponse> messagePage;
        if (beforeId != null) {
            messagePage = chatMessageService.getMessagesBefore(conversationId, beforeId, currentUser.getId(), size);
        } else {
            messagePage = chatMessageService.getConversationMessages(conversationId, currentUser.getId(), page, size);
        }
        
        return Result.success(messagePage.getRecords(), messagePage.getTotal(), 
                messagePage.getPages(), messagePage.getCurrent(), messagePage.getSize());
    }

    /**
     * 4.10.2 标记消息已读
     * PUT /api/conversations/{conversationId}/read
     */
    @Operation(summary = "标记消息已读", description = "将会话中的所有消息标记为已读")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @PutMapping("/{conversationId}/read")
    public Result<Void> markAsRead(
            @Parameter(description = "会话ID") @PathVariable Long conversationId) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        // 验证会话权限
        if (!conversationService.isParticipant(conversationId, currentUser.getId())) {
            return Result.failed("无权限操作此会话");
        }
        
        Conversation conversation = conversationService.getById(conversationId);
        if (conversation == null) {
            return Result.failed("会话不存在");
        }
        
        // 标记消息已读
        chatMessageService.markAllAsRead(conversationId, currentUser.getId());
        
        // 清空未读数
        if (currentUser.getId().equals(conversation.getHrUserId())) {
            conversationService.clearHrUnreadCount(conversationId);
        } else {
            conversationService.clearSeekerUnreadCount(conversationId);
        }
        
        return Result.success("已标记为已读");
    }

    /**
     * 4.10.2 撤回消息
     * DELETE /api/conversations/{conversationId}/messages/{messageId}
     */
    @Operation(summary = "撤回消息", description = "撤回2分钟内的消息")
    @PreAuthorize("hasAnyRole('HR', 'SEEKER', 'ADMIN')")
    @DeleteMapping("/{conversationId}/messages/{messageId}")
    public Result<Void> recallMessage(
            @Parameter(description = "会话ID") @PathVariable Long conversationId,
            @Parameter(description = "消息ID") @PathVariable Long messageId) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Result.failed("用户未登录");
        }
        
        chatMessageService.recallMessage(messageId, currentUser.getId());
        return Result.success("消息已撤回");
    }
}
