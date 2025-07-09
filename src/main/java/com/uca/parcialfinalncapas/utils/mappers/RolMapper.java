package com.uca.parcialfinalncapas.utils.mappers;

import com.uca.parcialfinalncapas.dto.response.RolResponse;
import com.uca.parcialfinalncapas.utils.enums.Rol;

public class RolMapper {
    public static RolResponse toDto(Rol rol) {
        return RolResponse.builder()
                .id(rol.getId())
                .build();
    }

    public static Rol toEntity(RolResponse rol) {
        return Rol.builder()
                .id(rol.getId())
                .build();
    }
}

