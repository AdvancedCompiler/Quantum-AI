package com.rf.AIquantum.dialogue.service;

import com.rf.AIquantum.dialogue.dao.model.DialogueEntity;
import com.rf.AIquantum.user.dao.model.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @Description: 对话相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface DialogueService {

    DialogueEntity save(DialogueEntity dialogueEntity);

    Page<DialogueEntity> findByPhoneAndStatusAndSearch(int pageNum, int pageSize, String phone, int status, String search);

    void insert(DialogueEntity dialogueEntity);
}
