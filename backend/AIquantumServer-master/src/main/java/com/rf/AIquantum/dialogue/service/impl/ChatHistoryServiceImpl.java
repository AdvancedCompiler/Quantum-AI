package com.rf.AIquantum.dialogue.service.impl;

import com.rf.AIquantum.dialogue.dao.model.ChatHistoryEntity;
import com.rf.AIquantum.dialogue.dao.model.DialogueEntity;
import com.rf.AIquantum.dialogue.dao.repository.ChatHistoryRepository;
import com.rf.AIquantum.dialogue.service.ChatHistoryService;
import com.rf.AIquantum.user.dao.model.UserEntity;
import com.rf.AIquantum.user.dao.repository.UserRepository;
import com.rf.AIquantum.user.service.UserService;
import com.rf.AIquantum.utils.Constant;
import com.rf.AIquantum.utils.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: 聊天记录相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Override
    public ChatHistoryEntity save(ChatHistoryEntity chatHistoryEntity) {
        return this.chatHistoryRepository.save(chatHistoryEntity);
    }

    @Override
    public List<ChatHistoryEntity> findChatHistoryByDialogueIdAndStatus(String dialogueId) {
        return this.chatHistoryRepository.findChatHistoryByDialogueIdAndStatus(dialogueId,1,Sort.by(Sort.Direction.DESC, "createTime"));
    }

    @Override
    public Page<ChatHistoryEntity> findByDialogueIdAndStatus(int pageNum, int pageSize, String dialogueId, int status) {
        Page<ChatHistoryEntity> page = this.chatHistoryRepository.findByDialogueIdAndStatus(dialogueId,status,PageRequestUtil.of(pageNum, pageSize));
        return page;
    }

    @Override
    public void deleteByDialogueIdAndCreateTime(String dialogueId, String createTime) {
        this.chatHistoryRepository.deleteByDialogueIdAndCreateTime(dialogueId,createTime);
    }

    @Override
    public void deleteByDialogueId(String dialogueId) {
        this.chatHistoryRepository.deleteByDialogueId(dialogueId);
    }
}
