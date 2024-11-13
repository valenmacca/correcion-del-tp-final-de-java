package valentino.coderhouse.jpa.repositories;

import valentino.coderhouse.jpa.entities.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
}