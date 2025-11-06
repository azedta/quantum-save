package com.quantumsave.quantum_save.dto;


import com.quantumsave.quantum_save.entity.ProfileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthDTO {

    private String email;
    private String password;
    private String token;


}
