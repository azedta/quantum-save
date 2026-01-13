package com.quantumsave.quantum_save.controller;


import com.quantumsave.quantum_save.dto.ApiResponse;
import com.quantumsave.quantum_save.dto.IncomeDTO;
import com.quantumsave.quantum_save.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<IncomeDTO> addIncome(@RequestBody IncomeDTO incomeDTO) {
        IncomeDTO saved = incomeService.addIncome(incomeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes() {
        List<IncomeDTO> incomes =  incomeService.getCurrentMonthIncomesForCurrentUser();
        return  ResponseEntity.ok(incomes);
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<ApiResponse<Object>> deleteIncome(@PathVariable Long incomeId) {
        incomeService.deleteIncome(incomeId);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Income deleted successfully")
                        .data(java.util.Map.of("incomeId", incomeId))
                        .timestamp(java.time.Instant.now())
                        .build()
        );
    }


}
