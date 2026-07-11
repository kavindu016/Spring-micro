package com.example.inventory_service;

import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repo.InventoryRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	// TO load dummy data
	@Bean
	public CommandLineRunner loadData(InventoryRepo inventoryRepo) {
		return args -> {
			Inventory inventory_1 = new Inventory();
			inventory_1.setSkuCode("Redmi Note 14");
			inventory_1.setQuantity(100);

			Inventory inventory_2 = new Inventory();
			inventory_2.setSkuCode("Redmi Note 13");
			inventory_2.setQuantity(0);

			inventoryRepo.save(inventory_1);
			inventoryRepo.save(inventory_2);
		};
	}
}
