package com.rf.AIquantum.dialogue.dao.repository;

import com.rf.AIquantum.base.repository.BaseRepository;
import com.rf.AIquantum.dialogue.dao.model.ChatHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Description: 聊天记录相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface ChatHistoryRepository extends BaseRepository<ChatHistoryEntity, String> {


    List<ChatHistoryEntity> findChatHistoryByDialogueIdAndStatus(String dialogueId, int status, Sort createTime);

    @Query(value = "select * from t_chat_history where dialogue_id = ?1 AND status = ?2 order by create_time desc",nativeQuery = true)
    Page<ChatHistoryEntity> findByDialogueIdAndStatus(String dialogueId, int status, PageRequest of);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM t_chat_history WHERE dialogue_id = ?1 AND create_time >= ?2  ", nativeQuery = true)
    void deleteByDialogueIdAndCreateTime(String dialogueId, String createTime);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM t_chat_history WHERE dialogue_id = ?1 ", nativeQuery = true)
    void deleteByDialogueId(String dialogueId);
}
