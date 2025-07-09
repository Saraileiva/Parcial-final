package com.uca.parcialfinalncapas.repository;

import com.uca.parcialfinalncapas.utils.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {
}

