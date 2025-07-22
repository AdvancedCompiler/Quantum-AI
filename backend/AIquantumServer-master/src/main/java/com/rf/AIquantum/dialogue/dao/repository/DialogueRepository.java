package com.rf.AIquantum.dialogue.dao.repository;

import com.rf.AIquantum.base.repository.BaseRepository;
import com.rf.AIquantum.dialogue.dao.model.DialogueEntity;
import com.rf.AIquantum.user.dao.model.UserEntity;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * @Description: 对话相关接口
 * @Author: zsy
 * @Date: 2024/12/4
 */
public interface DialogueRepository extends BaseRepository<DialogueEntity, String> {


    UserEntity findUserByPhone(String phone);

    @Query(value = "select * from t_dialogue_info where phone = ?1 " +
            " AND status = ?2 AND if( ?3 is not null and  ?3 != '', (dialogue_name like CONCAT('%',  ?3, '%')), 1 = 1) order by create_time desc",nativeQuery = true)
    Page<DialogueEntity> findByPhoneAndStatusAndSearch(String phone, int status, String search, PageRequest of);

    @Modifying
    @Query(value = " insert into t_dialogue_info(id,create_time,update_time,dialogue_name,phone,status) values " +
            "(:#{#dialogueEntity.id},:#{#dialogueEntity.createTime},:#{#dialogueEntity.updateTime},:#{#dialogueEntity.dialogueName}," +
            ":#{#dialogueEntity.phone},:#{#dialogueEntity.status}) ", nativeQuery = true)
    void insert(@Param("dialogueEntity") DialogueEntity dialogueEntity);
}
