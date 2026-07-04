package com.example.product_service.repo;

import com.example.product_service.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface Productrepo extends MongoRepository<Product,String> {

}
