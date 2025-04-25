package com_supermarket.controller;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.service.CaliforniaService;
import com_supermarket.service.HiperLibertadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    public ResponseEntity<Page<ProductPreviewResponseDto>> findAllProductsPageable(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        List<ProductPreviewResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaService.findAll());
        allProducts.addAll(hiperLibertadService.findAll());

        return paginateList(allProducts, pageable);
    }

    @GetMapping("/products/all")
    public ResponseEntity<List<ProductPreviewResponseDto>> findAllProducts() {
        List<ProductPreviewResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaService.findAll());
        allProducts.addAll(hiperLibertadService.findAll());

        return ResponseEntity.ok(allProducts);
    }

    @GetMapping("/products/search")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProducts(
            @RequestParam String name,
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        List<ProductPreviewResponseDto> results = new ArrayList<>();
        results.addAll(californiaService.findProducts(name, Pageable.unpaged()).getContent());
        results.addAll(hiperLibertadService.findProducts(name, Pageable.unpaged()).getContent());

        return results.isEmpty() ?
                ResponseEntity.noContent().build() :
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
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProductsWithPriceDecreaseLastWeek(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        // Primero obtén todas las entidades (sin paginar)
        List<ProductPreviewResponseDto> californiaProducts = californiaService
                .findProductsWithPriceDecreaseLastWeek();

        List<ProductPreviewResponseDto> hiperLibertadProducts = hiperLibertadService
                .findProductsWithPriceDecreaseLastWeek();

        // Combina las listas
        List<ProductPreviewResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaProducts);
        allProducts.addAll(hiperLibertadProducts);

        // Aplica la paginación a la lista combinada
        return paginateList(allProducts, pageable);
    }

    private ResponseEntity<Page<ProductPreviewResponseDto>> paginateList(
            List<ProductPreviewResponseDto> items, Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());

        List<ProductPreviewResponseDto> pageContent = items.subList(start, end);

        return ResponseEntity.ok(new PageImpl<>(
                pageContent,
                pageable,
                items.size()
        ));
    }


}