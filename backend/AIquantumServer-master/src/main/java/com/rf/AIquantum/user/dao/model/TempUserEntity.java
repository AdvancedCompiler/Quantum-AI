package com.rf.AIquantum.user.dao.model;

import com.rf.AIquantum.base.model.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author:zzf
 * @Date:2025/3/21:16:29
 * @Description:
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_temp_user_info")
@EqualsAndHashCode(callSuper=true)
@org.hibernate.annotations.Table(appliesTo = "t_temp_user_info", comment = "用户临时信息表")
public class TempUserEntity extends BaseEntity {
    @ApiModelProperty(value = "ip",required = true)
    @Column(name = "ip",unique = true,columnDefinition = "varchar(100) comment 'ip'")
    private String ip;
    @ApiModelProperty("用户名")
    @Column(name = "username",columnDefinition = "varchar(100) comment '用户名'")
    private String username;
    @ApiModelProperty("剩余次数")
    @Column(name = "remaining_count",columnDefinition = "int comment '剩余次数'")
    private int remainingCount;
}
