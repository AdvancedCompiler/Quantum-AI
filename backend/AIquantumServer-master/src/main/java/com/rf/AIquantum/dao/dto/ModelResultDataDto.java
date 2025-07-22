package com.rf.AIquantum.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JpaDto
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModelResultDataDto {

    private String id;

    private String event;

    private String data;
}
