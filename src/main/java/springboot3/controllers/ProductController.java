package springboot3.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springboot3.dtos.ProductRecordDto;
import springboot3.models.ProductModel;
import springboot3.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ProductController {
    ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    @PostMapping("/products")
    public ResponseEntity<ProductModel> saveProduct(@RequestBody @Valid ProductRecordDto productRecordDto) {
        ProductModel productModel = new ProductModel();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(productModel));
    }

    @GetMapping("products")
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> productModelList = productRepository.findAll();
        if (!productModelList.isEmpty()) {
            for (ProductModel product : productModelList) {
                UUID id = product.getIdProduct();
                product.add(linkTo(methodOn(ProductController.class).getProductById(id)).withSelfRel());
            }
        }
        return ResponseEntity.ok().body(productModelList);
    }

    @GetMapping("products/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);

        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        product.get().add(linkTo(methodOn(ProductController.class).getAllProducts()).withRel("Products List:"));
        return ResponseEntity.ok(product.get());
    }

    @PutMapping("products/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id,
                                                @RequestBody @Valid ProductRecordDto productRecordDto) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        var productModel = product.get();
        BeanUtils.copyProperties(productRecordDto, productModel);
        return ResponseEntity.ok().body(productRepository.save(productModel));
    }
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable(value = "id") UUID id) {
        Optional<ProductModel> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        productRepository.delete(product.get());
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully.");
    }
}
