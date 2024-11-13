package valentino.coderhouse.jpa.controllers;

import valentino.coderhouse.jpa.dto.ErrorResponseDto;
import valentino.coderhouse.jpa.entities.Client;
import valentino.coderhouse.jpa.services.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clients")
public class ClientController {

    @Autowired
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener todos los clientes", description = "Obtiene una lista de todos los clientes registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succesful operation"),
            @ApiResponse(responseCode = "400", description = "invalid request"),
    })
    @GetMapping
    public ResponseEntity<List<Client>> getClients() {
        return ResponseEntity.ok(this.service.getClients());
    }

    @Operation(summary = "Obtener un cliente por ID", description = "Obtiene los datos de un cliente específico usando su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }
            )})
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable String id) {
        Client client = service.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Buscar cliente por número de documento", description = "Obtiene los datos de un cliente utilizando su número de documento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }
            )})
    @GetMapping("/doc/{docNumber}")
    public ResponseEntity<Client> getClientByDocNumber(@PathVariable String docNumber) {
        Client client = service.findClientByDocNumber(docNumber);
        return ResponseEntity.ok(client);
    }

    @Operation(summary = "Crear un nuevo cliente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos incompletos o incorrectos)",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
            })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createClient(
            @RequestBody
            @io.swagger.v3.oas.annotations.media.Schema(example = "{\n" +
                    "  \"id\": \"49d7fb2e-1435-41a2-8cc2-020bfeeb4151\",\n" +
                    "  \"name\": \"John Doe\",\n" +
                    "  \"email\": \"johndoe@example.com\",\n" +
                    "  \"docNumber\": \"12345678\"\n" +
                    "}")
            Client client) {
        try {
            Client savedClient = this.service.createClient(client);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedClient);
        } catch (IllegalArgumentException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "Parámetros inválidos",
                    "client");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto(
                    String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                    HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "Error inesperado del servidor",
                    "internal_error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Actualizar un cliente existente por ID", description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Succesful update Client"),
            @ApiResponse(responseCode = "404", description = "Client no found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }
            )})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable String id, @RequestBody Client client) {
        try {
            service.updateClient(id, client);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404", "Not Found", "Cliente no encontrado", "id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Eliminar un cliente por ID", description = "Elimina un cliente del sistema utilizando su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Succesful delete Client"),
            @ApiResponse(responseCode = "404", description = "Client no found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }
            )})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable String id) {
        try {
            service.deleteClient(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404", "Not Found", "Cliente no encontrado", "id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
