package com.example.order_service.service;

import com.example.order_service.dto.OrderLineItemsDto;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderLineItems;
import com.example.order_service.repo.Orderrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final Orderrepo orderrepo;

    public  void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        orderrepo.save(order);
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;

    }
}
