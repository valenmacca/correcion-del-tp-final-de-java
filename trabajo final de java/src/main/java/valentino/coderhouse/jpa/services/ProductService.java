package valentino.coderhouse.jpa.services;
import valentino.coderhouse.jpa.entities.Product;
import valentino.coderhouse.jpa.repositories.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product save(@Valid Product product) {
        try {
            return productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product data is not valid", e);
        } catch (Exception e) {
            throw new RuntimeException("Error to save the product", e);
        }
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product updateProduct(String id, Product newProduct) {
        return productRepository.findById(id).map(product -> {
            System.out.println("Actualizando producto con ID: " + product.getId() + " con nuevo precio: " + newProduct.getPrice());
            product.setDescription(newProduct.getDescription());
            product.setCodigo(newProduct.getCodigo());
            product.setStock(newProduct.getStock());
            product.setPrice(newProduct.getPrice());
            return productRepository.save(product);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public void deleteProduct(String id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

}
