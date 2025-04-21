package com_supermarket.controller;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.service.CaliforniaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/california")
public class CaliforniaController {
    private final CaliforniaService californiaService;

    public CaliforniaController(CaliforniaService californiaService) {
        this.californiaService = californiaService;
    }

    @PostMapping()
    public ResponseEntity<List<ProductResponseDto>> updateProducts() {
        californiaService.scrapeProducts();
        return ResponseEntity.ok().body(null);
    }
    //TODO UNA FUNCION PARA TRAER LA CANTIDAD DE PRODUCTOS TOTALES

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> findAllPageable(@PageableDefault(page = 0, size = 21)Pageable pageable) {
        Page<ProductResponseDto> products = californiaService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDto>> findAll() {
        List<ProductResponseDto> products = californiaService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> findProducts(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "21") int size) {

        Page<ProductResponseDto> products = californiaService.findProducts(name, page, size);

        if (products.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-decrease-last-week")
    public ResponseEntity<Page<ProductResponseDto>> findProductsWithPriceDecreaseLastWeek(@PageableDefault(page = 0, size = 21) Pageable pageable) {
        Page<ProductResponseDto> products = californiaService.findProductsWithPriceDecreaseLastWeek(pageable);
        return ResponseEntity.ok(products);
    }
}
