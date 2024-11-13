package valentino.coderhouse.jpa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDto {

    @NotBlank
    @Schema(description = "Código de estado HTTP", example = "404")
    private String statusCode;

    @NotBlank
    @Schema(description = "Estado de la respuesta", example = "Not Found")
    private String status;

    @NotBlank
    @Schema(description = "Mensaje de error detallado", example = "Cliente no encontrado")
    private String message;

    @Schema(description = "Campo relacionado al error", example = "id")
    private String field;

    public ErrorResponseDto(String statusCode, String status, String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
        this.field = null;
    }

    public ErrorResponseDto(String message) {
        this.message = message;
    }

}
