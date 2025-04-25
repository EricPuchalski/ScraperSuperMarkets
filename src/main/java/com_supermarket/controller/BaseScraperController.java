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

    @PostMapping
    public ResponseEntity<Void> updateProducts() {
        storeService.scrapeProducts();
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductPreviewResponseDto>> findAllPageable(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {
        return ResponseEntity.ok(storeService.findAll(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductPreviewResponseDto>> findAll() {
        return ResponseEntity.ok(storeService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProducts(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        Page<ProductPreviewResponseDto> products = storeService.findProducts(name, pageable);
        return products.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(products);
    }

    @GetMapping("/price-decrease-last-week")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProductsWithPriceDecreaseLastWeek(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {
        return ResponseEntity.ok(storeService.findProductsWithPriceDecreaseLastWeekPageable(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> findById(@PathVariable String id) {
        return ResponseEntity.ok(storeService.findById(id));
    }
}
