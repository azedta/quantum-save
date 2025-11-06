package com.quantumsave.quantum_save.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private String name;
    private Long profileId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type;
    private String icon;

}
