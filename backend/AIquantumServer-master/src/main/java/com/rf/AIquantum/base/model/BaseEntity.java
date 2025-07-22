package com.rf.AIquantum.base.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author zzf
 * @description:
 * @date 2021/1/18 19:13
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({AuditingEntityListener.class})
@MappedSuperclass
public class BaseEntity implements Serializable {
    @Getter
    @Id
    @GenericGenerator(name = "jpa-uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @GeneratedValue(generator = "jpa-uuid")
    @Column(name = "id", columnDefinition = "varchar(36) COMMENT '数据ID'")
    @NotNull(groups = Update.class)
    private String id;

    @Column(name = "create_time", columnDefinition = "varchar(36) COMMENT '创建时间'")
    private String createTime;


    @Column(name = "update_time", columnDefinition = "varchar(36) COMMENT '更新时间'")
    private String updateTime;



    public @interface Update {
    }
}
