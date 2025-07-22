package com.rf.AIquantum.dialogue.dao.model;

import com.rf.AIquantum.base.model.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author zsy
 * @description:对话信息表
 * @date 2021/6/17 15:55
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_dialogue_info")
@EqualsAndHashCode(callSuper=true)
@org.hibernate.annotations.Table(appliesTo = "t_dialogue_info", comment = "对话信息表")
public class DialogueEntity extends BaseEntity {

    @Column(name = "dialogue_name", columnDefinition = "varchar(60) not null comment '对话名称'")
    private String dialogueName;

    @Column(name = "phone", columnDefinition = "varchar(36) not null comment '所属用户手机号'")
    private String phone;

    @Column(name = "status", columnDefinition = "int(2) not null comment  '状态(0：删除；1：正常)'")
    private int status;

}
