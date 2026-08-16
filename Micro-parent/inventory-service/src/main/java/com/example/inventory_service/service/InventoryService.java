package com.example.inventory_service.service;

import com.example.inventory_service.dto.InventoryResponse;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repo.InventoryRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    @Transactional(readOnly = true)
    @SneakyThrows
    public List<InventoryResponse> isStock(List<String> skuCode) {
        log.info("wait Started");
        // This is only for simulation slow service to check the Time Out in Breaker
        // Thread.sleep(10000);
        log.info("wait End");
        return inventoryRepo.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder()
                            .skuCode(inventory.getSkuCode())
                            .isStock(inventory.getQuantity() > 0)
                            .build()
                )
                .toList();

    }
}
