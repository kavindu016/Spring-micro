package com.example.product_service.service;

import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import com.example.product_service.repo.Productrepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// This will automatically create the constructor
@RequiredArgsConstructor
// To see logs
@Slf4j
public class Productservice {

    private final Productrepo productrepo;

    public void CreateProduct(ProductRequest productRequest) {
        // Map the DTO and the model
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price((productRequest.getPrice()))
                .build();

        productrepo.save(product);
        log.info("Product" + product.getId() + " saved");

    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productrepo.findAll();
        
        return products.stream().map(this::mapToProductResponse).toList();
    }

    // we can just sent the request using model..but using different -
    // - DTO for that is good way
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
}
