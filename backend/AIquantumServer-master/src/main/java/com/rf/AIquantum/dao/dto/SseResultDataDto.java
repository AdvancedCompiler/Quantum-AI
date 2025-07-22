package com.rf.AIquantum.dao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JpaDto
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SseResultDataDto {

    private String dialogueId;

    private String type;

    private String content;
}
