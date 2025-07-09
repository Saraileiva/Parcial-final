package com.uca.parcialfinalncapas.service.impl;

import com.uca.parcialfinalncapas.dto.request.UserCreateRequest;
import com.uca.parcialfinalncapas.dto.request.UserUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.UserResponse;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.exceptions.UserNotFoundException;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.UserService;
import com.uca.parcialfinalncapas.utils.mappers.UserMapper;
import lombok.AllArgsConstructor; // OJO: Con la inyección de PasswordEncoder, puede que necesites un constructor explícito
import org.springframework.security.crypto.password.PasswordEncoder; // <-- Nueva importación
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
//@AllArgsConstructor // Mejor un constructor explícito cuando se inyectan más cosas
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <-- Inyectar PasswordEncoder

    // Constructor explícito para inyección de dependencias
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse findByCorreo(String correo) {
        return UserMapper.toDTO(userRepository.findByCorreo(correo)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con correo: " + correo)));
    }

    @Override
    public UserResponse save(UserCreateRequest userCreateRequest) { // Cambié el nombre del parámetro para claridad

        if (userRepository.findByCorreo(userCreateRequest.getCorreo()).isPresent()) {
            throw new UserNotFoundException("Ya existe un usuario con el correo: " + userCreateRequest.getCorreo());
        }

        // Mapear el DTO a la entidad User
        User userToSave = UserMapper.toEntityCreate(userCreateRequest);
        // ENCRIPTAR LA CONTRASEÑA ANTES DE GUARDAR
        userToSave.setPassword(passwordEncoder.encode(userCreateRequest.getPassword()));

        return UserMapper.toDTO(userRepository.save(userToSave));
    }

    @Override
    public UserResponse update(UserUpdateRequest userUpdateRequest) { // Cambié el nombre del parámetro para claridad
        User existingUser = userRepository.findById(userUpdateRequest.getId())
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el ID: " + userUpdateRequest.getId()));

        // Mapear los campos actualizables (excluyendo la contraseña si no se desea actualizar)
        User updatedUserEntity = UserMapper.toEntityUpdate(userUpdateRequest);

        // Si la contraseña se va a actualizar, encriptarla.
        // Asumo que UserUpdateRequest puede contener un nuevo password.
        // Si no, y este update es solo para otros campos, puedes omitir esto.
        if (userUpdateRequest.getPassword() != null && !userUpdateRequest.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userUpdateRequest.getPassword()));
        }

        // Transferir los campos actualizados al usuario existente (UserMapper.toEntityUpdate podría hacer esto)
        existingUser.setNombre(updatedUserEntity.getNombre());
        existingUser.setApellido(updatedUserEntity.getApellido());
        existingUser.setCorreo(updatedUserEntity.getCorreo());
        existingUser.setRol(updatedUserEntity.getRol()); // Asegúrate que el rol también se actualice si es necesario

        return UserMapper.toDTO(userRepository.save(existingUser));
    }


    @Override
    public void delete(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new UserNotFoundException("No se encontró un usuario con el ID: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> findAll() {
        return UserMapper.toDTOList(userRepository.findAll());
    }
}