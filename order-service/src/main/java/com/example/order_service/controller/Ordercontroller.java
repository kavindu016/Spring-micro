package com.example.order_service.controller;

import com.example.order_service.dto.OrderRequest;
import com.example.order_service.model.Order;
import com.example.order_service.repo.Orderrepo;
import com.example.order_service.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class Ordercontroller {

    private final OrdersService ordersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody OrderRequest orderRequest) {
        ordersService.placeOrder(orderRequest);
        return "Order Placed Successfully";
    }
}
