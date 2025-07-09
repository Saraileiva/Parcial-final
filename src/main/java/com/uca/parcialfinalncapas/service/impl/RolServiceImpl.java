package com.uca.parcialfinalncapas.service.impl;


import com.uca.parcialfinalncapas.dto.response.RolResponse;
import com.uca.parcialfinalncapas.service.RolService;
import com.uca.parcialfinalncapas.utils.mappers.RolMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {
    private RolRepository rolRepository;

    @Autowired
    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public RolResponse getRole(UUID id) {
        return RolMapper.toDto(rolRepository.findById(id)
                .orElseThrow(() -> new RolNotFoundException("Rol not found"))
        );
    }
}

