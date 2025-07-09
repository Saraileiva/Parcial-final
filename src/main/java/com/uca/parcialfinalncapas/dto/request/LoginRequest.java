package com.uca.parcialfinalncapas.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String correo; // <-- Usar 'correo' en lugar de 'username'
    private String password;
}
