package com_supermarket.controller;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.repository.ScraperRepository;
import com_supermarket.service.AdminService;
import com_supermarket.service.CaliforniaService;
import com_supermarket.service.HiperLibertadService;
import com_supermarket.service.impl.BaseScraperService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final CaliforniaService californiaService;
    private final HiperLibertadService hiperLibertadService;
    private final AdminService adminService;

    public AdminController(CaliforniaService californiaService,
                           HiperLibertadService hiperLibertadService, AdminService adminService) {
        this.californiaService = californiaService;
        this.hiperLibertadService = hiperLibertadService;
        this.adminService = adminService;
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

        // Divide el tamaño de página para cada servicio
        Pageable halfPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize() / 2,
                pageable.getSort()
        );

        Page<ProductPreviewResponseDto> californiaPage = californiaService.findAll(halfPageable);
        Page<ProductPreviewResponseDto> hiperLibertadPage = hiperLibertadService.findAll(halfPageable);

        // Combina resultados
        List<ProductPreviewResponseDto> combinedContent = Stream.concat(
                californiaPage.getContent().stream(),
                hiperLibertadPage.getContent().stream()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(
                combinedContent,
                pageable,
                californiaPage.getTotalElements() + hiperLibertadPage.getTotalElements()
        ));
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

    @GetMapping("/products/price-decrease")
    public ResponseEntity<Page<ProductPreviewResponseDto>> findProductsWithPriceDecreaseLastWeek(
            @PageableDefault(page = 0, size = 21) Pageable pageable) {

        // Ejecuta ambas consultas en paralelo con el pageable completo
        CompletableFuture<Page<ProductPreviewResponseDto>> californiaFuture = CompletableFuture.supplyAsync(() ->
                californiaService.findProductsWithPriceDecreasePageable(pageable)
        );

        CompletableFuture<Page<ProductPreviewResponseDto>> hiperLibertadFuture = CompletableFuture.supplyAsync(() ->
                hiperLibertadService.findProductsWithPriceDecreasePageable(pageable)
        );

        // Espera y combina los resultados
        try {
            Page<ProductPreviewResponseDto> californiaPage = californiaFuture.join();
            Page<ProductPreviewResponseDto> hiperLibertadPage = hiperLibertadFuture.join();

            List<ProductPreviewResponseDto> allResults = new ArrayList<>();
            allResults.addAll(californiaPage.getContent());
            allResults.addAll(hiperLibertadPage.getContent());

            // Ordena los resultados combinados según los criterios de paginación
            allResults.sort(Comparator.comparing(ProductPreviewResponseDto::getId));

            // Crea una nueva página con los resultados combinados
            Page<ProductPreviewResponseDto> combinedPage = new PageImpl<>(
                    allResults,
                    pageable,
                    californiaPage.getTotalElements() + hiperLibertadPage.getTotalElements()
            );

            return ResponseEntity.ok(combinedPage);
        } catch (CompletionException e) {
            // Maneja las excepciones de CompletableFuture
            e.printStackTrace(); // Agrega esto para ver el stack trace en los logs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // Maneja cualquier otra excepción
            e.printStackTrace(); // Agrega esto para ver el stack trace en los logs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

    @GetMapping("/pages")
    public ResponseEntity<List<String>> getPages() {
        return ResponseEntity.ok(adminService.findPages());
    }

}