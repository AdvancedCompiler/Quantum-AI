package com.rf.AIquantum.dialogue.service.impl;

import com.rf.AIquantum.dialogue.dao.model.DialogueEntity;
import com.rf.AIquantum.dialogue.dao.repository.DialogueRepository;
import com.rf.AIquantum.dialogue.service.DialogueService;
import com.rf.AIquantum.user.dao.model.UserEntity;
import com.rf.AIquantum.user.dao.repository.UserRepository;
import com.rf.AIquantum.user.service.UserService;
import com.rf.AIquantum.utils.Constant;
import com.rf.AIquantum.utils.PageRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @Description: 对话相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
@Service
public class DialogueServiceImpl implements DialogueService {

    @Autowired
    private DialogueRepository dialogueRepository;

    @Override
    public DialogueEntity save(DialogueEntity dialogueEntity) {
        return this.dialogueRepository.save(dialogueEntity);
    }

    @Override
    public Page<DialogueEntity> findByPhoneAndStatusAndSearch(int pageNum, int pageSize, String phone, int status, String search) {
        Page<DialogueEntity> page = this.dialogueRepository.findByPhoneAndStatusAndSearch(phone,status,search, PageRequestUtil.of(pageNum, pageSize));
        return page;
    }

    @Transactional
    @Modifying
    @Query
    @Override
    public void insert(DialogueEntity dialogueEntity) {
        this.dialogueRepository.insert(dialogueEntity);
    }
}
