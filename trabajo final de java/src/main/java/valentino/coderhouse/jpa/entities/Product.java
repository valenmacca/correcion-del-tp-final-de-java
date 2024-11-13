package valentino.coderhouse.jpa.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "PRODUCT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    @Schema(description = "Unique ID of the product", accessMode = Schema.AccessMode.READ_ONLY, example = "99887766-81b7-4924-952e-8d3fe108ab8f")
    private String id;

    @Column(name = "DESCRIPTION", nullable = false)
    @Schema(description = "Description of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "resma A5")
    private String description;

    @Column(name = "CODIGO", nullable = false)
    @Schema(description = "Code of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "sdf4sd4ws3")
    private String codigo;

    @Column(name = "STOCK", nullable = false)
    @Schema(description = "Stock available for this product", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private int stock;

    @Column(name = "PRICE", nullable = false)
    @Schema(description = "Price of the product", requiredMode = Schema.RequiredMode.REQUIRED, example = "750.40")
    private double price;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("product")
    @Schema(description = "List of invoice details associated with this product", example = "null")
    private List<InvoiceDetail> details;

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }

    public void decreaseStock(int quantity) {
        this.stock -= quantity;
    }

    public boolean isInStock(int quantity) {
        return this.stock >= quantity;
    }

}
