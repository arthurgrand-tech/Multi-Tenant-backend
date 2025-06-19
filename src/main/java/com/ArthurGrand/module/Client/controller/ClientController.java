package com.ArthurGrand.module.Client.controller;

import com.ArthurGrand.dto.ApiResponse;
import com.ArthurGrand.dto.ClientDTO;
import com.ArthurGrand.module.Client.service.ClientService;
import com.ArthurGrand.common.exception.ClientNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<?>> saveClient(@Valid @RequestBody ClientDTO clientDTO, BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            Map<String, Object> error = new HashMap<>();
            error.put("timestamp", Instant.now());
            error.put("status", HttpStatus.BAD_REQUEST.value());
            error.put("error", "Validation Error");
            error.put("message", errors);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Validation Error", error));
        }

        try {
            ClientDTO savedClient = clientService.saveClient(clientDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("Client saved successfully", savedClient));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to save client: " + e.getMessage(), null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getAllClients() {
        try {
            List<ClientDTO> clients = clientService.getAllClients();
            return ResponseEntity.ok(new ApiResponse<>("Clients fetched successfully", clients));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to fetch clients: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<ClientDTO>> getClientById(@PathVariable Integer id) {
        try {
            ClientDTO client = clientService.getClientById(id);
            return ResponseEntity.ok(new ApiResponse<>("Client fetched successfully", client));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Client not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to fetch client: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<ClientDTO>> updateClient(@PathVariable Integer id,
                                                               @Valid @RequestBody ClientDTO clientDTO,
                                                               BindingResult result) {
        if (result.hasErrors()) {
            String errors = result.getAllErrors()
                    .stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Validation Error: " + errors, null));
        }

        try {
            ClientDTO updatedClient = clientService.updateClient(id, clientDTO);
            return ResponseEntity.ok(new ApiResponse<>("Client updated successfully", updatedClient));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Client not found", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to update client: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<ApiResponse<Void>> deleteClient(@PathVariable Integer id) {
        try {
            clientService.deleteClient(id);
            return ResponseEntity.ok(new ApiResponse<>("Client deleted successfully", null));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Client not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to delete client: " + e.getMessage(), null));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getActiveClients() {
        try {
            List<ClientDTO> activeClients = clientService.getActiveClients();
            return ResponseEntity.ok(new ApiResponse<>("Active clients fetched successfully", activeClients));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to fetch active clients: " + e.getMessage(), null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> searchClients(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) Boolean isActive) {
        try {
            List<ClientDTO> clients = clientService.searchClients(clientName, email, industry, isActive);
            return ResponseEntity.ok(new ApiResponse<>("Clients search completed successfully", clients));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to search clients: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id:\\d+}/activate")
    public ResponseEntity<ApiResponse<ClientDTO>> activateClient(@PathVariable Integer id) {
        try {
            ClientDTO client = clientService.activateClient(id);
            return ResponseEntity.ok(new ApiResponse<>("Client activated successfully", client));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Client not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to activate client: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id:\\d+}/deactivate")
    public ResponseEntity<ApiResponse<ClientDTO>> deactivateClient(@PathVariable Integer id) {
        try {
            ClientDTO client = clientService.deactivateClient(id);
            return ResponseEntity.ok(new ApiResponse<>("Client deactivated successfully", client));
        } catch (ClientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("Client not found", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("Failed to deactivate client: " + e.getMessage(), null));
        }
    }
}