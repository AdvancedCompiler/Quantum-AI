package com.rf.AIquantum.dialogue.service;

import com.rf.AIquantum.dialogue.dao.model.ChatHistoryEntity;
import com.rf.AIquantum.user.dao.model.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @Description: 聊天记录相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface ChatHistoryService {

    ChatHistoryEntity save(ChatHistoryEntity chatHistoryEntity);

    List<ChatHistoryEntity> findChatHistoryByDialogueIdAndStatus(String dialogueId);

    Page<ChatHistoryEntity> findByDialogueIdAndStatus(int pageNum, int pageSize, String dialogueId, int status);

    void deleteByDialogueIdAndCreateTime(String dialogueId, String createTime);

    void deleteByDialogueId(String dialogueId);
}
