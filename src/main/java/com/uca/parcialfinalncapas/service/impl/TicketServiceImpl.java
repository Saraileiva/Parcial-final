package com.uca.parcialfinalncapas.service.impl;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponseList;
import com.uca.parcialfinalncapas.entities.Ticket;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.exceptions.TicketNotFoundException;
import com.uca.parcialfinalncapas.exceptions.UserNotFoundException;
import com.uca.parcialfinalncapas.repository.TicketRepository;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.enums.Rol; // Tu enum Rol
import com.uca.parcialfinalncapas.utils.mappers.TicketMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TicketResponse createTicket(TicketCreateRequest ticketCreateRequest) { // Cambié el nombre del parámetro
        // Obtener el usuario solicitante por correo
        var usuarioSolicitante = userRepository.findByCorreo(ticketCreateRequest.getCorreoUsuario())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante no encontrado con correo: " + ticketCreateRequest.getCorreoUsuario()));

        // Obtener el usuario de soporte por correo (puede ser nulo en la creación inicial si no se asigna)
        User usuarioSoporte = null;
        if (ticketCreateRequest.getCorreoSoporte() != null && !ticketCreateRequest.getCorreoSoporte().isEmpty()) {
            usuarioSoporte = userRepository.findByCorreo(ticketCreateRequest.getCorreoSoporte())
                    .orElseThrow(() -> new UserNotFoundException("Usuario asignado no encontrado con correo: " + ticketCreateRequest.getCorreoSoporte()));

            // Verificar si el usuario asignado es un técnico
            if (usuarioSoporte != null && !usuarioSoporte.getRol().equals(Rol.TECH)) { // <-- Usar .equals(Rol.TECH)
                throw new BadTicketRequestException("El usuario asignado no es un técnico de soporte");
            }
        }

        // Mapear a la entidad Ticket usando los IDs de los usuarios
        var ticketGuardado = ticketRepository.save(TicketMapper.toEntityCreate(
                ticketCreateRequest,
                usuarioSolicitante.getId(),
                (usuarioSoporte != null ? usuarioSoporte.getId() : null) // Pasa null si no hay técnico
        ));

        // Mapear de la entidad guardada a TicketResponse
        return TicketMapper.toDTO(
                ticketGuardado,
                usuarioSolicitante.getCorreo(),
                (usuarioSoporte != null ? usuarioSoporte.getCorreo() : null)
        );
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(TicketUpdateRequest ticketUpdateRequest) { // Cambié el nombre del parámetro
        Ticket ticketExistente = ticketRepository.findById(ticketUpdateRequest.getId())
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con ID: " + ticketUpdateRequest.getId()));

        // El usuario solicitante no debería cambiar en un update de ticket
        var usuarioSolicitante = userRepository.findById(ticketExistente.getUsuarioId())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante del ticket no encontrado"));

        User usuarioSoporte = null;
        if (ticketUpdateRequest.getCorreoSoporte() != null && !ticketUpdateRequest.getCorreoSoporte().isEmpty()) {
            usuarioSoporte = userRepository.findByCorreo(ticketUpdateRequest.getCorreoSoporte())
                    .orElseThrow(() -> new UserNotFoundException("Usuario asignado no encontrado con correo: " + ticketUpdateRequest.getCorreoSoporte()));

            if (usuarioSoporte != null && !usuarioSoporte.getRol().equals(Rol.TECH)) { // <-- Usar .equals(Rol.TECH)
                throw new BadTicketRequestException("El usuario asignado no es un técnico de soporte");
            }
        }

        // Mapear los campos actualizados y guardar
        var ticketGuardado = ticketRepository.save(TicketMapper.toEntityUpdate(
                ticketUpdateRequest,
                (usuarioSoporte != null ? usuarioSoporte.getId() : null), // Pasa el ID del técnico
                ticketExistente // Pasa la entidad existente para que el mapper pueda actualizarla
        ));

        return TicketMapper.toDTO(
                ticketGuardado,
                usuarioSolicitante.getCorreo(),
                (usuarioSoporte != null ? usuarioSoporte.getCorreo() : null)
        );
    }

    @Override
    public void deleteTicket(Long id) {
        var ticketExistente = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con ID: " + id));

        ticketRepository.delete(ticketExistente);
    }

    @Override
    public TicketResponse getTicketById(Long id) {
        var ticketExistente = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket no encontrado con ID: " + id));

        var usuarioSolicitante = userRepository.findById(ticketExistente.getUsuarioId())
                .orElseThrow(() -> new UserNotFoundException("Usuario solicitante no encontrado"));

        // El técnico asignado puede ser nulo
        User usuarioSoporte = null;
        if (ticketExistente.getTecnicoAsignadoId() != null) {
            usuarioSoporte = userRepository.findById(ticketExistente.getTecnicoAsignadoId())
                    .orElse(null); // No lanzar excepción si el técnico asignado no se encuentra, solo dejarlo nulo
        }

        return TicketMapper.toDTO(
                ticketExistente,
                usuarioSolicitante.getCorreo(),
                (usuarioSoporte != null ? usuarioSoporte.getCorreo() : null)
        );
    }

    @Override
    public List<TicketResponseList> getAllTickets() {
        return TicketMapper.toDTOList(ticketRepository.findAll());
    }

    // --- Métodos adicionales para el filtrado de USER ---
    public List<TicketResponse> getTicketsByRequesterCorreo(String correoUsuario) {
        User requester = userRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con correo: " + correoUsuario));
        List<Ticket> tickets = ticketRepository.findByUsuarioId(requester.getId());

        // Mapear de Ticket a TicketResponse (necesitas un mapper para esto)
        // Esto asume que TicketMapper tiene un método toResponse que toma un Ticket y correos
        return tickets.stream().map(ticket -> {
            User soporte = null;
            if (ticket.getTecnicoAsignadoId() != null) {
                soporte = userRepository.findById(ticket.getTecnicoAsignadoId()).orElse(null);
            }
            return TicketMapper.toDTO(ticket, requester.getCorreo(), (soporte != null ? soporte.getCorreo() : null));
        }).collect(Collectors.toList());
    }
}