package com_supermarket.controller;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.service.CaliforniaService;
import com_supermarket.service.HiperLibertadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final CaliforniaService californiaService;
    private final HiperLibertadService hiperLibertadService;

    public AdminController(CaliforniaService californiaService,
                           HiperLibertadService hiperLibertadService) {
        this.californiaService = californiaService;
        this.hiperLibertadService = hiperLibertadService;
    }

    @PostMapping("/update-all")
    public ResponseEntity<Map<String, String>> updateAllProducts() {
        californiaService.scrapeProducts();
        hiperLibertadService.scrapeProducts();

        return ResponseEntity.ok(Map.of(
                "message", "Todos los productos han sido actualizados correctamente",
                "california", "ok",
                "hiperlibertad", "ok"
        ));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> findAllProductsPageable(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        List<ProductResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaService.findAll());
        allProducts.addAll(hiperLibertadService.findAll());

        return paginateList(allProducts, pageable);
    }

    @GetMapping("/products/all")
    public ResponseEntity<List<ProductResponseDto>> findAllProducts() {
        List<ProductResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaService.findAll());
        allProducts.addAll(hiperLibertadService.findAll());

        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductResponseDto>> findProducts(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        List<ProductResponseDto> results = new ArrayList<>();
        results.addAll(californiaService.findProducts(name, Pageable.unpaged()).getContent());
        results.addAll(hiperLibertadService.findProducts(name, Pageable.unpaged()).getContent());

        return results.isEmpty() ?
                ResponseEntity.notFound().build() :
                paginateList(results, pageable);
    }

    @GetMapping("/products/count")
    public ResponseEntity<Map<String, Long>> getProductsCount() {
        long californiaCount = californiaService.findAll().size();
        long hiperLibertadCount = hiperLibertadService.findAll().size();

        return ResponseEntity.ok(Map.of(
                "california", californiaCount,
                "hiperlibertad", hiperLibertadCount,
                "total", californiaCount + hiperLibertadCount
        ));
    }

    @GetMapping("/products/price-decrease-last-week")
    public ResponseEntity<Page<ProductResponseDto>> findProductsWithPriceDecreaseLastWeek(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        // Obtener todos los productos con descuento de California (paginados)
        Page<ProductResponseDto> californiaProducts = californiaService
                .findProductsWithPriceDecreaseLastWeek(pageable);

        // Obtener todos los productos con descuento de HiperLibertad (paginados)
        Page<ProductResponseDto> hiperLibertadProducts = hiperLibertadService
                .findProductsWithPriceDecreaseLastWeek(pageable);

        // Combinar los contenidos
        List<ProductResponseDto> combinedContent = new ArrayList<>();
        combinedContent.addAll(californiaProducts.getContent());
        combinedContent.addAll(hiperLibertadProducts.getContent());

        // Calcular el total combinado
        long totalElements = californiaProducts.getTotalElements() +
                hiperLibertadProducts.getTotalElements();

        // Crear la página de respuesta
        Page<ProductResponseDto> resultPage = new PageImpl<>(
                combinedContent,
                pageable,
                totalElements
        );

        return ResponseEntity.ok(resultPage);
    }

    private ResponseEntity<Page<ProductResponseDto>> paginateList(
            List<ProductResponseDto> items, Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());

        List<ProductResponseDto> pageContent = items.subList(start, end);

        return ResponseEntity.ok(new PageImpl<>(
                pageContent,
                pageable,
                items.size()
        ));
    }
}