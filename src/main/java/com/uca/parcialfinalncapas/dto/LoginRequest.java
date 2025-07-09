package com.uca.parcialfinalncapas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import static com.uca.parcialfinalncapas.utils.Constants.INVALID_EMAIL;
import static com.uca.parcialfinalncapas.utils.Regexp.REGEXP_PASSWORD;

@Data
@Builder
public class LoginRequest {
    @Email(message = INVALID_EMAIL)
    private String email;
    @Pattern(regexp = REGEXP_PASSWORD)
    private String password;
}
