package com.uca.parcialfinalncapas.service;

import com.uca.parcialfinalncapas.dto.response.RolResponse;

import java.util.UUID;

public interface RolService {
    RolResponse getRole(UUID id);
}

