package valentino.coderhouse.jpa.controllers;

import valentino.coderhouse.jpa.dto.ErrorResponseDto;
import valentino.coderhouse.jpa.entities.Product;
import valentino.coderhouse.jpa.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener todos los productos", description = "Obtiene una lista de todos los productos disponibles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "succeses operation"),
            @ApiResponse(responseCode = "404", description = "Not founded products")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Product>> getAll() {
        List<Product> products = service.getProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(summary = "Obtener un producto por ID", description = "Obtiene un producto específico mediante su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "succeses operation"),
            @ApiResponse(responseCode = "404", description = "Not founded product")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            Product product = service.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404", "Not Found", "Producto no encontrado", "id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Crear un nuevo producto", description = "Agrega un nuevo producto al sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "succeses new product"),
            @ApiResponse(responseCode = "400", description = "invalid params")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody Product product) {
        try {
            Product newProduct = service.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("400", "Bad Request", "Parámetros inválidos", "product");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

    }

    @Operation(summary = "Actualizar un producto existente", description = "Actualiza los datos de un producto existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "succeses update product"),
            @ApiResponse(responseCode = "404", description = "Not founded product")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @RequestBody Product product) {
        try {
            product.setId(id);
            Product updatedProduct = service.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404", "Not Found", "Producto no encontrado", "id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Eliminar un producto por ID", description = "Elimina un producto del sistema mediante su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "product deleted "),
            @ApiResponse(responseCode = "404", description = "Not founded product")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            service.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404", "Not Found", "Producto no encontrado", "id");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

}
