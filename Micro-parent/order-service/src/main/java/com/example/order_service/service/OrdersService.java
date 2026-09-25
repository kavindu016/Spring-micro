package com.example.order_service.service;

import brave.Span;
import brave.Tracer;
import com.example.order_service.dto.InventoryResponse;
import com.example.order_service.dto.OrderLineItemsDto;
import com.example.order_service.dto.OrderRequest;
import com.example.order_service.event.OrderPlaceEvent;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderLineItems;
import com.example.order_service.repo.Orderrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdersService {

    private final Orderrepo orderrepo;
    private final WebClient.Builder webClient;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlaceEvent> kafkaTemplate;

    public  String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        Span inventorySeriveLookUp = tracer.nextSpan().name("InventorySeriveLookUp");

        try( Tracer.SpanInScope spanInScope =  tracer.withSpanInScope(inventorySeriveLookUp.start())) {
            InventoryResponse[] inventoryResponses = webClient.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            assert inventoryResponses != null;

            boolean allInStock = inventoryResponses.length == skuCodes.size()
                    && Arrays.stream(inventoryResponses)
                    .allMatch(r -> Boolean.TRUE.equals(r.isStock()));

            if (allInStock) {
                orderrepo.save(order);
                kafkaTemplate.send("notificationTopic", new OrderPlaceEvent(order.getOrderId()));
                return "Order Placed Successfully";
            } else {
                throw new IllegalArgumentException("Product is not in the stock");
            }
        }finally {
            inventorySeriveLookUp.finish();
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;

    }
}
