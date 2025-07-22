package com.rf.AIquantum.param.model;

import com.rf.AIquantum.base.model.BaseEntity;
import lombok.*;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author:zzf
 * @Date:2022/7/1:17:54
 * @Description:系统参数
 */
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "t_system_param")
@org.hibernate.annotations.Table(appliesTo = "t_system_param", comment = "系统参数表")
public class ParamEntry  extends BaseEntity {


    @Column(name = "param_name",columnDefinition = "varchar(50) not null comment '参数名称'")
    private String paramName ;

    @Column(name = "param_value",columnDefinition = "varchar(200) not null comment '参数值'")
    private String paramValue ;

    @Column(name = "param_desc",columnDefinition = "varchar(200) comment '参数释义'")
    private String paramDesc;

    @Column(name = "param_type",columnDefinition = "varchar(20) comment '参数类型'")
    private String paramType;

    @Column(name = "sort_value",columnDefinition = "int comment '排序值'")
    private String sortValue;

}
