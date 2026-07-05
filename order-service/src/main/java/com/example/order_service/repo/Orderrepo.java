package com.example.order_service.repo;

import com.example.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Orderrepo extends JpaRepository<Order, Long> {
}
