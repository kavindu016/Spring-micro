package com.example.product_service.controller;

import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.model.Product;
import com.example.product_service.service.Productservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class Productcontroller {

    private final Productservice productservice;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public  void createProduct(@RequestBody ProductRequest productRequest) {
        productservice.CreateProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getProducts() {
        return productservice.getAllProducts();
    }
}
