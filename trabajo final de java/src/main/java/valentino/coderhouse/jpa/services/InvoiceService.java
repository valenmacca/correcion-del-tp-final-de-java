package valentino.coderhouse.jpa.services;

import valentino.coderhouse.jpa.entities.Client;
import valentino.coderhouse.jpa.entities.Invoice;
import valentino.coderhouse.jpa.entities.InvoiceDetail;
import valentino.coderhouse.jpa.entities.Product;
import valentino.coderhouse.jpa.exceptions.InsufficientStockException;
import valentino.coderhouse.jpa.repositories.InvoiceRepository;
import valentino.coderhouse.jpa.repositories.ClientRepository;
import valentino.coderhouse.jpa.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MainService mainService;

    public Invoice createInvoice(Invoice invoice) throws InsufficientStockException {
        log.debug("Validating invoice...");
        validateInvoice(invoice);

        log.debug("Fetching current date...");
        LocalDateTime currentDateTime = mainService.getCurrentArgentinaDateTime();
        invoice.setCreatedAt(currentDateTime);

        for (InvoiceDetail detail : invoice.getDetails()) {
            detail.setPrice(detail.getProduct().getPrice());
            detail.setInvoice(invoice);
        }

        log.debug("Calculating total for the invoice...");
        double total = calculateTotal(invoice);
        invoice.setTotal(total);

        log.debug("Calculating total number of products...");
        int totalProducts = calculateTotalProducts(invoice);

        log.debug("Saving invoice to the database...");
        return invoiceRepository.save(invoice);
    }

    private void validateInvoice(Invoice invoice) {

        Optional<Client> client = clientRepository.findById(invoice.getClient().getId());
        if (client.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no existe");
        }
        invoice.setClient(client.get());

        for (InvoiceDetail detail : invoice.getDetails()) {
            Optional<Product> product = productRepository.findById(detail.getProduct().getId());
            if (product.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no existe");
            }

            if (detail.getAmount() > product.get().getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cantidad mayor al stock disponible");
            }

            detail.setProduct(product.get());
            product.get().setStock(product.get().getStock() - detail.getAmount());
            productRepository.save(product.get());
        }
    }

    public double calculateTotal(Invoice invoice) {
        double total = 0;
        for (InvoiceDetail detail : invoice.getDetails()) {
            total += detail.getAmount() * detail.getPrice();
        }
        return total;
    }

    public int calculateTotalProducts(Invoice invoice) {
        int totalProducts = 0;
        for (InvoiceDetail detail : invoice.getDetails()) {
            totalProducts += detail.getAmount();
        }
        return totalProducts;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(id).orElse(null);
    }

    public Invoice updateInvoice(String id, Invoice invoiceDetails) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
        if (invoiceOptional.isPresent()) {
            Invoice invoice = invoiceOptional.get();

            invoice.setClient(invoiceDetails.getClient());

            Optional<Client> clientOptional = clientRepository.findById(invoiceDetails.getClient().getId());
            if (clientOptional.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente no existe");
            }

            Client client = clientOptional.get();
            invoice.setClient(client);

            invoice.getDetails().clear();

            for (InvoiceDetail detail : invoiceDetails.getDetails()) {
                Optional<Product> productOptional = productRepository.findById(detail.getProduct().getId());
                if (productOptional.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no existe");
                }

                Product product = productOptional.get();
                if (detail.getAmount() > product.getStock()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cantidad mayor al stock disponible");
                }

                product.setStock(product.getStock() - detail.getAmount());
                productRepository.save(product);

                detail.setInvoice(invoice);

                invoice.getDetails().add(detail);
            }

            double total = calculateTotal(invoice);
            invoice.setTotal(total);

            return invoiceRepository.save(invoice);
        } else {
            return null;
        }
    }

    public boolean deleteInvoice(String id) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findById(id);
        if (invoiceOptional.isPresent()) {
            invoiceRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

}
