package com_supermarket.controller;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import com_supermarket.service.ScraperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class BaseScraperController<S extends ScraperService> {
    protected final S storeService;

    protected BaseScraperController(S storeService) {
        this.storeService = storeService;
    }

    @PostMapping("products")
    public ResponseEntity<Void> updateProducts() {
        storeService.scrapeProducts();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("products")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findAllPageable(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {
        return ResponseEntity.ok(storeService.findAll(pageable));
    }

    @GetMapping("products/all")
    public ResponseEntity<List<ProductPreviewResponseDto>> findAll() {
        return ResponseEntity.ok(storeService.findAll());
    }

    @GetMapping("products/search")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProducts(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 21, sort = "currentPrice") Pageable pageable) {

        Page<ProductPreviewResponseDto> products = storeService.findProducts(name, pageable);
        return products.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(products);
    }

    @GetMapping("products/price-decrease")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProductsWithPriceDecreaseLastWeek(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {
        return ResponseEntity.ok(storeService.findProductsWithPriceDecreasePageable(pageable));
    }

    @GetMapping("products/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(storeService.findById(id));
    }
}
