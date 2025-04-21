package com_supermarket.controller;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.service.CaliforniaService;
import com_supermarket.service.HiperLibertadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public AdminController(CaliforniaService californiaService, HiperLibertadService hiperLibertadService) {
        this.californiaService = californiaService;
        this.hiperLibertadService = hiperLibertadService;
    }

    @PostMapping("/update-all")
    public ResponseEntity<Map<String, String>> updateAllProducts() {
        californiaService.scrapeProducts();
        hiperLibertadService.scrapeProducts();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Todos los productos han sido actualizados correctamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductResponseDto>> findAllProductsPageable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "21") int size) {

        // Obtenemos todos los productos de ambas tiendas
        List<ProductResponseDto> allProducts = new ArrayList<>();
        allProducts.addAll(californiaService.findAll());
        allProducts.addAll(hiperLibertadService.findAll());

        // Creamos una paginación manual de la lista unificada
        return createPageResponse(allProducts, page, size);
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "21") int size) {

        // Buscamos productos en ambas tiendas
        Page<ProductResponseDto> californiaProducts = californiaService.findProducts(name, page, size);
        Page<ProductResponseDto> hiperLibertadProducts = hiperLibertadService.findProducts(name, page, size);

        // Unificamos los resultados
        List<ProductResponseDto> combinedResults = new ArrayList<>();
        combinedResults.addAll(californiaProducts.getContent());
        combinedResults.addAll(hiperLibertadProducts.getContent());

        // Si no hay resultados, devolvemos 404
        if (combinedResults.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Paginamos la lista unificada
        return createPageResponse(combinedResults, page, size);
    }

    @GetMapping("/products/count")
    public ResponseEntity<Map<String, Long>> getProductsCount() {
        Map<String, Long> counts = new HashMap<>();

        List<ProductResponseDto> californiaProducts = californiaService.findAll();
        List<ProductResponseDto> hiperLibertadProducts = hiperLibertadService.findAll();

        counts.put("california", (long) californiaProducts.size());
        counts.put("hiperlibertad", (long) hiperLibertadProducts.size());
        counts.put("total", (long) (californiaProducts.size() + hiperLibertadProducts.size()));

        return ResponseEntity.ok(counts);
    }

    // Método auxiliar para crear una respuesta paginada a partir de una lista
    private ResponseEntity<Page<ProductResponseDto>> createPageResponse(List<ProductResponseDto> allProducts, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProducts.size());

        List<ProductResponseDto> pageContent;
        if (start <= end) {
            pageContent = allProducts.subList(start, end);
        } else {
            pageContent = new ArrayList<>();
        }

        Page<ProductResponseDto> productPage = new PageImpl<>(
                pageContent,
                pageable,
                allProducts.size()
        );

        return ResponseEntity.ok(productPage);
    }

    // VER LOS PRODUCTOS QUE BASJARON DE PRECIO EN LOS ULTIMOS 7 DIAS
//
//    @GetMapping("/products/price-decrease-last-week")
//    public ResponseEntity<Page<ProductResponseDto>> findProductsWithPriceDecreaseLastWeek(@PageableDefault(page = 0, size = 21) Pageable pageable) {
//        // Obtenemos todos los productos de ambas tiendas
//        List<ProductResponseDto> allProducts = new ArrayList<>();
//        allProducts.addAll(californiaService.findProductsWithPriceDecreaseLastWeek();
//        allProducts.addAll(hiperLibertadService.findProductsWithPriceDecreaseLastWeek(PageRequest.of(page, size)).getContent());
//
//
//        return createPageResponse(allProducts, page, d);
//    }
}