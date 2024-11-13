package valentino.coderhouse.jpa.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name= "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    @Schema(description = "Unique ID of the client", accessMode = Schema.AccessMode.READ_ONLY, example = "0124529f-81b7-4924-952e-8d3fe108ab8f")
    private String id;

    @Column(name = "NAME", nullable = false)
    @Schema(description = "First name of the client", requiredMode = Schema.RequiredMode.REQUIRED, example = "valentino")
    private String name;

    @Column(name = "LASTNAME", nullable = false)
    @Schema(description = "Last name of the client", requiredMode = Schema.RequiredMode.REQUIRED, example = "maccaroni")
    private String lastName;

    @Column(name = "DOCNUMBER", nullable = false)
    @Schema(description = "Document number of the client", requiredMode = Schema.RequiredMode.REQUIRED, example = "46573166")
    private String docNumber;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("client")
    @Schema(description = "List of invoices associated with the client")
    private List<Invoice> invoices= new ArrayList<>();

    public Client(String name, String lastName, String docNumber) {
        this.name = name;
        this.lastName = lastName;
        this.docNumber = docNumber;
    }

}
