package com.rf.AIquantum.user.dao.model;

import com.rf.AIquantum.base.model.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author zsy
 * @description:用户信息表
 * @date 2021/6/17 15:55
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_user_info")
@EqualsAndHashCode(callSuper=true)
@org.hibernate.annotations.Table(appliesTo = "t_user_info", comment = "用户信息表")
public class UserEntity extends BaseEntity {

    @Column(name = "user_name", columnDefinition = "varchar(50) not null comment '用户名'")
    private String userName;

    @Column(name = "phone", unique = true,columnDefinition = "varchar(36) not null comment '手机号（唯一、登录名）'")
    private String phone;

    @Column(name = "password", columnDefinition = "varchar(32) not null comment '密码'")
    private String password;

    @Column(name = "role_type", columnDefinition = "varchar(2) default '1' comment  '角色(默认为1 1普通用户；2 管理员)'")
    private String roleType;

    @Column(name = "avatar", columnDefinition = "varchar(255) comment '头像URL'")
    private String avatar;

}
