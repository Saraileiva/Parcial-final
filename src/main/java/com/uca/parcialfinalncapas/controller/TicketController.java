package com.uca.parcialfinalncapas.controller;

import com.uca.parcialfinalncapas.dto.request.TicketCreateRequest;
import com.uca.parcialfinalncapas.dto.request.TicketUpdateRequest;
import com.uca.parcialfinalncapas.dto.response.GeneralResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponse;
import com.uca.parcialfinalncapas.dto.response.TicketResponseList;
import com.uca.parcialfinalncapas.entities.Ticket;
import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.exceptions.BadTicketRequestException;
import com.uca.parcialfinalncapas.repository.TicketRepository;
import com.uca.parcialfinalncapas.repository.UserRepository;
import com.uca.parcialfinalncapas.service.TicketService;
import com.uca.parcialfinalncapas.utils.ResponseBuilderUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@AllArgsConstructor
public class TicketController {
    private TicketService ticketService;
    private UserRepository userRepository;
    private TicketRepository ticketRepository;

    // GET /api/tickets: USER ve solo sus tickets, TECH ve todos
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> getAllTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName(); // Correo es el username

        List<TicketResponseList> tickets; // Usamos TicketResponseList para el listado

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TECH"))) {
            // Si es TECH, obtén todos los tickets
            tickets = ticketService.getAllTicketsForList(); // Asume un método que devuelve TicketResponseList
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            // Si es USER, obtén solo sus tickets
            User currentUser = userRepository.findByEmail(authenticatedCorreo)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));

            // Aquí el servicio debería filtrar por el ID del usuario
            tickets = ticketService.getTicketsByUsuarioIdForList(currentUser.getId());
        } else {
            return ResponseBuilderUtil.buildResponse("Acceso denegado", HttpStatus.FORBIDDEN, null);
        }

        return ResponseBuilderUtil.buildResponse("Tickets obtenidos correctamente",
                tickets.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK,
                tickets);
    }

    // GET /api/tickets/{id}: USER ve solo su ticket, TECH ve cualquier ticket
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> getTicketById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName();

        // Primero, intentar obtener el ticket. El servicio ya debería manejar el mapeo a DTO.
        TicketResponse ticket = ticketService.getTicketById(id);
        if (ticket == null) {
            throw new BadTicketRequestException("Ticket no encontrado");
        }

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
            // Si es USER, verifica que el ticket le pertenezca
            User currentUser = userRepository.findByEmail(authenticatedCorreo)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));

            // Para esta validación, necesitas la entidad Ticket para comparar IDs de usuario.
            Optional<Ticket> actualTicketEntity = ticketRepository.findById(id);

            if (actualTicketEntity.isEmpty() || !actualTicketEntity.get().getUsuarioId().equals(currentUser.getId())) {
                return ResponseBuilderUtil.buildResponse("Acceso denegado: Este ticket no te pertenece", HttpStatus.FORBIDDEN, null);
            }
        }
        // Si es TECH, o es USER y el ticket le pertenece, se permite el acceso
        return ResponseBuilderUtil.buildResponse("Ticket encontrado", HttpStatus.OK, ticket);
    }

    // POST /api/tickets: USER y TECH pueden crear tickets
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'TECH')")
    public ResponseEntity<GeneralResponse> createTicket(@Valid @RequestBody TicketCreateRequest ticketRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedCorreo = authentication.getName();

        User currentUser = userRepository.findByEmail(authenticatedCorreo)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found for correo: " + authenticatedCorreo));

        // El ID del usuario solicitante es el ID del usuario autenticado
        Long usuarioSolicitanteId = currentUser.getId();
        Long tecnicoAsignadoId = null; // Inicialmente, no hay técnico asignado al crear el ticket

        // Si el usuario autenticado es TECH, pueden asignar un técnico al crear.
        // Pero tu `TicketCreateRequest` no parece tener un campo para `tecnicoAsignadoId`.
        // Si deseas que los TECH puedan asignarlo al crear, deberías añadirlo al DTO.
        // Por ahora, lo dejo como null al crear.

        TicketResponse createdTicket = ticketService.createTicket(ticketRequest, usuarioSolicitanteId, tecnicoAsignadoId);

        return ResponseBuilderUtil.buildResponse("Ticket creado correctamente", HttpStatus.CREATED, createdTicket);
    }

    // PUT /api/tickets: Solo TECH puede actualizar tickets
    @PutMapping
    @PreAuthorize("hasRole('TECH')")
    public ResponseEntity<GeneralResponse> updateTicket(@Valid @RequestBody TicketUpdateRequest ticketRequest) {
        // En tu TicketUpdateRequest, asumo que viene el ID del ticket a actualizar
        // y opcionalmente el ID del técnico a asignar/re-asignar.
        // El `TicketUpdateRequest` tiene `estado` y `descripcion`.

        // Aquí el servicio manejará la lógica de actualización, incluyendo
        // la posible asignación o re-asignación de un técnico.
        // Necesitas el ID del técnico que viene en el DTO o se asigna en el servicio.
        // Si TicketUpdateRequest incluye un campo para tecnicoAsignadoId, lo usarías.
        // Ejemplo: ticketService.updateTicket(ticketRequest, ticketRequest.getTecnicoId());

        TicketResponse updatedTicket = ticketService.updateTicket(ticketRequest); // Asume que el servicio maneja la lógica de técnico
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