package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponseList;
import com.uca.parcialfinalncapas.entities.Ticket; // Tu entidad Ticket
import com.uca.parcialfinalncapas.entities.User;   // Tu entidad User
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.repository.TicketRepository; // Necesario para filtrar tickets por usuario
import com.uca.parcialfinalncapas.repository.UserRepository;     // Necesario para obtener el usuario autenticado
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // <-- Importante
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // Si tu Ticket tiene creationDate
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {
    private TicketService ticketService;
    private UserRepository userRepository; // Necesario para obtener el User autenticado
    private TicketRepository ticketRepository; // Necesario para filtrar tickets por requester

    // GET /api/tickets: USER ve solo sus tickets, TECH ve todos
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> getAllTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName(); // El correo del usuario autenticado

        List<TicketResponse> tickets;

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECH"))) {
            // Si es TECH, obtén todos los tickets
            tickets = ticketService.getAllTickets();
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            // Si es USER, obtén solo sus tickets
            User currentUser = userRepository.findByCorreo(authenticatedCorreo)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));
            List<Ticket> userTickets = ticketRepository.findByRequester(currentUser);

            // Mapea las entidades Ticket a TicketResponse. Ajusta esto según tu TicketMapper.java
            tickets = userTickets.stream()
                    .map(ticket -> {
                        TicketResponse tr = new TicketResponse();
                        tr.setId(ticket.getId());
                        tr.setTitle(ticket.getTitle());
                        tr.setDescription(ticket.getDescription());
                        tr.setStatus(ticket.getStatus());
                        tr.setCreationDate(ticket.getCreationDate()); // Asumiendo que TicketResponse tiene creationDate
                        // Asegúrate de que TicketResponse pueda mostrar el correo del solicitante si es necesario
                        if (ticket.getRequester() != null) {
                            // Si TicketResponse tiene un campo 'requesterCorreo' o similar
                            // Puedes añadir esto a tu TicketResponse DTO
                            // tr.setRequesterCorreo(ticket.getRequester().getCorreo());
                        }
                        return tr;
                    })
                    .collect(Collectors.toList());
        } else {
            return ResponseBuilderUtil.buildResponse("Acceso denegado", HttpStatus.FORBIDDEN, null);
        }

        return ResponseBuilderUtil.buildResponse("Tickets obtenidos correctamente",
                tickets.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK, // Usar NO_CONTENT si la lista está vacía
                tickets);
    }

    // GET /api/tickets/{id}: USER ve solo su ticket, TECH ve cualquier ticket
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> getTicketById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName();

        TicketResponse ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new BadTicketRequestException("Ticket no encontrado");
        }

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            // Si es USER, verifica que el ticket le pertenezca
            User currentUser = userRepository.findByCorreo(authenticatedCorreo)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));

            // Aquí necesitas obtener la entidad Ticket para comparar el requester
            Optional<Ticket> actualTicketEntity = ticketRepository.findById(id);

            if (actualTicketEntity.isEmpty() || !actualTicketEntity.get().getRequester().getId().equals(currentUser.getId())) {
                return ResponseBuilderUtil.buildResponse("Acceso denegado: Este ticket no te pertenece", HttpStatus.FORBIDDEN, null);
            }
        }
        // Si es TECH, o es USER y el ticket le pertenece, se permite el acceso
        return ResponseBuilderUtil.buildResponse("Ticket found", HttpStatus.OK, ticket);
    }

    // POST /api/tickets: USER y TECH pueden crear tickets
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> createTicket(@Valid @RequestBody TicketCreateRequest ticketRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName();

        User currentUser = userRepository.findByCorreo(authenticatedCorreo)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));

        // **IMPORTANTE:** Aquí necesitas asegurarte de que tu TicketService.createTicket()
        // asigne el `currentUser` como `requester` del nuevo `Ticket`.
        // Opción 1 (Modificar TicketService): Tu servicio podría recibir el objeto User o su ID.
        // Ejemplo: TicketResponse createdTicket = ticketService.createTicket(ticketRequest, currentUser);
        // O si tu servicio es lo suficientemente inteligente para obtenerlo del SecurityContextHolder.
        //
        // Opción 2 (Ajuste en el controlador si TicketService no se modifica):
        // Si tu TicketService.createTicket solo toma el DTO, podrías hacer la asignación aquí
        // antes de guardar, si TicketService te devuelve la entidad o puedes mapear.
        // Esto depende de cómo está implementado tu TicketService y TicketMapper.
        // Para simplificar y no cambiar la firma de tu TicketService si no es necesario,
        // asumo que tu TicketService ya sabe cómo obtener el usuario autenticado para el ticket
        // o lo añadirás en la implementación del servicio.
        TicketResponse createdTicket = ticketService.createTicket(ticketRequest); // Asume que el servicio maneja la asignación del requester

        return ResponseBuilderUtil.buildResponse("Ticket creado correctamente", HttpStatus.CREATED, createdTicket);
    }

    // PUT /api/tickets: Solo TECH puede actualizar tickets
    @PutMapping
    @PreAuthorize("hasRole('TECH')")
    public ResponseEntity<GeneralResponse> updateTicket(@Valid @RequestBody TicketUpdateRequest ticket) {
        TicketResponse updatedTicket = ticketService.updateTicket(ticket);
        return ResponseBuilderUtil.buildResponse("Ticket actualizado correctamente", HttpStatus.OK, updatedTicket);
    }

    // DELETE /api/tickets/{id}: Solo TECH puede eliminar tickets
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TECH')")
    public ResponseEntity<GeneralResponse> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseBuilderUtil.buildResponse("Ticket eliminado correctamente", HttpStatus.OK, null);
    }
}