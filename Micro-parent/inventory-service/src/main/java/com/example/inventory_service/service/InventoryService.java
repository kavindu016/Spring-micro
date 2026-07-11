package com.example.inventory_service.service;

import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repo.InventoryRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepo inventoryRepo;

    @Transactional(readOnly = true)
    public boolean isStock(String skuCode) {
        return inventoryRepo.findBySkuCode(skuCode).isPresent();
    }
}
