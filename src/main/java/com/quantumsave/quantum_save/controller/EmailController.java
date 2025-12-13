package com.quantumsave.quantum_save.controller;

import com.quantumsave.quantum_save.service.EmailExpenseExcelService;
import com.quantumsave.quantum_save.service.EmailIncomeExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {

    private final EmailIncomeExcelService emailIncomeExcelService;
    private final EmailExpenseExcelService emailExpenseExcelService;


    @GetMapping("/income-excel")
    public ResponseEntity<String> emailIncomeExcel() {
        emailIncomeExcelService.sendIncomeExcelToCurrentUser();
        return ResponseEntity.ok("Income details emailed successfully");
    }

    @GetMapping("/expense-excel")
    public ResponseEntity<String> emailExpenseExcel() {
        emailExpenseExcelService.sendExpenseExcelToCurrentUser();
        return ResponseEntity.ok("Expense details emailed successfully");
    }

}
