package com.quantumsave.quantum_save.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {

    private String token;
    private ProfileDTO user;

    // frontend uses this to show "verify your email" banner
    private boolean isActive;

    // optional UX message
    private String message;
}
