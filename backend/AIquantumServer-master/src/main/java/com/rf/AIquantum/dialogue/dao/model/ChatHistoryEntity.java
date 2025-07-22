package com.rf.AIquantum.dialogue.dao.model;

import com.rf.AIquantum.base.model.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author zsy
 * @description:聊天记录表
 * @date 2021/6/17 15:55
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_chat_history")
@EqualsAndHashCode(callSuper=true)
@org.hibernate.annotations.Table(appliesTo = "t_chat_history", comment = "聊天记录表")
public class ChatHistoryEntity extends BaseEntity {

    @Column(name = "dialogue_id", columnDefinition = "varchar(36) not null comment '关联对话ID'")
    private String dialogueId;

    @Column(name = "role", columnDefinition = "varchar(50) not null comment  '消息角色(取值范围为 system、user 或 assistant)'")
    private String role;

    @Column(name = "content", columnDefinition = "text not null comment '消息内容'")
    private String content;

    @Column(name = "image", columnDefinition = "varchar(255) comment '图片URL'")
    private String image;

    @Column(name = "status", columnDefinition = "int(2) not null comment  '状态(0：删除；1：正常)'")
    private int status;

    @Column(name = "endorse", columnDefinition = "int(2) not null comment  '是否赞同(1：未评价（默认）；2：赞同；3：不赞同)'")
    private int endorse;

    @Column(name = "feedback", columnDefinition = "varchar(200) comment  '反馈意见'")
    private String feedback;

}
