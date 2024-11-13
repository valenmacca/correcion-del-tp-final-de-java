package valentino.coderhouse.jpa.controllers;

import valentino.coderhouse.jpa.dto.ErrorResponseDto;
import valentino.coderhouse.jpa.entities.Invoice;
import valentino.coderhouse.jpa.exceptions.InsufficientStockException;
import valentino.coderhouse.jpa.services.InvoiceService;
import valentino.coderhouse.jpa.services.MainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/invoices")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private MainService mainService;

    @Operation(summary = "Crear una nueva factura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (datos incompletos o incorrectos)",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }),

            @ApiResponse(responseCode = "409", description = "El cliente o producto no existen o no hay suficiente stock",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
    })
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createInvoice(
            @RequestBody
            @io.swagger.v3.oas.annotations.media.Schema(example = "{\n" +
                    "  \"client\": {\n" +
                    "    \"id\": \"49d7fb2e-1435-41a2-8cc2-020bfeeb4151\"\n" +
                    "  },\n" +
                    "  \"details\": [\n" +
                    "    {\n" +
                    "      \"product\": {\n" +
                    "        \"id\": \"0cccbc88-0793-42f0-b76f-ee7bdeedcedd\"\n" +
                    "      },\n" +
                    "      \"amount\": 4,\n" +
                    "      \"price\": 750.4\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}")
            Invoice invoice) {

        if (invoice.getClient() == null) {
            log.error("Error: El cliente no fue proporcionado en la factura");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                            HttpStatus.BAD_REQUEST.getReasonPhrase(),
                            "El cliente es obligatorio",
                            "client"));
        }

        if (invoice.getDetails() == null || invoice.getDetails().isEmpty()) {
            log.error("Error: Los detalles de la factura están vacíos o no fueron proporcionados");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                            HttpStatus.BAD_REQUEST.getReasonPhrase(),
                            "Los detalles de la factura son obligatorios",
                            "details"));
        }

        for (var detail : invoice.getDetails()) {
            if (detail.getProduct() == null) {
                log.error("Error: No se proporcionó un producto en uno de los detalles de la factura");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponseDto(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "Cada detalle debe tener un producto asociado",
                                "product"));
            }
            if (detail.getAmount() <= 0) {
                log.error("Error: La cantidad de uno de los productos es inválida: {}", detail.getAmount());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponseDto(String.valueOf(HttpStatus.BAD_REQUEST.value()),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                "La cantidad del producto debe ser mayor a 0",
                                "amount"));
            }
            detail.setInvoice(invoice);
        }

        try {
            LocalDateTime currentDateTime = mainService.getCurrentArgentinaDateTime();
            invoice.setCreatedAt(currentDateTime);
            log.info("Fecha de creación de la factura: {}", currentDateTime);

            Invoice createdInvoice = invoiceService.createInvoice(invoice);

            double totalAmount = invoiceService.calculateTotal(createdInvoice);
            int totalProducts = invoiceService.calculateTotalProducts(createdInvoice);

            Map<String, Object> response = new HashMap<>();
            response.put("invoice", createdInvoice);
            response.put("totalProducts", totalProducts);
            response.put("totalAmount", totalAmount);

            log.info("Factura creada exitosamente con ID: {}", createdInvoice.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InsufficientStockException e) {
            log.error("Stock insuficiente para uno de los productos de la factura. Detalle: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.CONFLICT.value()),
                            HttpStatus.CONFLICT.getReasonPhrase(),
                            e.getMessage(),
                            "stock"));

        } catch (ResponseStatusException e) {
            log.error("Error en validaciones de la factura: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(new ErrorResponseDto(String.valueOf(e.getStatusCode().value()),
                            e.getReason(),
                            e.getReason(), "validation_error"));

        } catch (Exception e) {
            log.error("Error inesperado al crear la factura", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                            "Error inesperado del servidor. Inténtalo más tarde.",
                            "internal_error"));
        }
    }

    @Operation(summary = "Obtener todas las facturas")
    @ApiResponses (value = {
            @ApiResponse(responseCode = "200", description = "Facturas obtenidas exitosamente"),
            @ApiResponse(responseCode = "404", description = "No se encontraron las facturas",
                content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
    })
    @GetMapping
    public ResponseEntity<?> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        if (invoices.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.NOT_FOUND.value()),
                            HttpStatus.NOT_FOUND.getReasonPhrase(),
                            "No se encontraron todas las facturas",
                            "invoices"));
        }
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Obtener una factura por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getInvoiceById(@PathVariable String id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (invoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND.getReasonPhrase(), "Factura no encontrada", "id"));
        }
        return ResponseEntity.ok(invoice);
    }

    @Operation(summary = "Actualizar una factura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) }),
            @ApiResponse(responseCode = "409", description = "Conflicto en la actualización de la factura: Stock insuficiente",
                    content = { @Content(mediaType = "application/json", schema = @Schema(example = "{\n" +
                            "  \"statusCode\": \"409\",\n" +
                            "  \"status\": \"Conflict\",\n" +
                            "  \"message\": \"Cantidad mayor al stock disponible\",\n" +
                            "  \"field\": \"stock\"\n" +
                            "}")
                    )}),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvoice(
            @PathVariable String id,
            @RequestBody
            @io.swagger.v3.oas.annotations.media.Schema(example = "{\n" +
                    "  \"client\": {\n" +
                    "    \"id\": \"123e4567-e89b-12d3-a456-426614174000\"\n" +
                    "  },\n" +
                    "  \"details\": [\n" +
                    "    {\n" +
                    "      \"product\": {\n" +
                    "        \"id\": \"270a05aa-7c34-4f14-9d5d-f877974c98f4\"\n" +
                    "      },\n" +
                    "      \"amount\": 5,\n" +
                    "      \"price\": 750.4\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}")
            Invoice invoiceDetails) {

        Invoice updatedInvoice = invoiceService.updateInvoice(id, invoiceDetails);
        if (updatedInvoice == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND.getReasonPhrase(), "Factura no encontrada", "id"));
        }
        double totalAmount = invoiceService.calculateTotal(updatedInvoice);
        int totalProducts = invoiceService.calculateTotalProducts(updatedInvoice);

        Map<String, Object> response = new HashMap<>();
        response.put("invoice", updatedInvoice);
        response.put("totalProducts", totalProducts);
        response.put("totalAmount", totalAmount);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Eliminar una factura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Factura eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)) })
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String id) {
        boolean deleted = invoiceService.deleteInvoice(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponseDto(String.valueOf(HttpStatus.NOT_FOUND.value()), HttpStatus.NOT_FOUND.getReasonPhrase(), "Factura no encontrada", "id"));
        }
        return ResponseEntity.noContent().build();
    }

}
