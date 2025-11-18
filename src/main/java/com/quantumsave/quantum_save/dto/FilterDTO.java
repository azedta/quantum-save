package com.quantumsave.quantum_save.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {

    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField; // Either by date, amount or name
    private String sortOrder; // Either ascending or descending
}
