package com_supermarket.controller;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.service.HiperLibertadService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hiperlibertad")
public class HiperLibertadController {
    private final HiperLibertadService hiperLibertadService;

    public HiperLibertadController(HiperLibertadService hiperLibertadService) {
        this.hiperLibertadService = hiperLibertadService;
    }

    @PostMapping()
    public ResponseEntity<List<ProductResponseDto>> updateProducts() {
        hiperLibertadService.scrapeProducts();
        return ResponseEntity.ok().body(null);
    }
    //TODO UNA FUNCION PARA TRAER LA CANTIDAD DE PRODUCTOS TOTALES

    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> findAllPageable(@PageableDefault(page = 0, size = 21)Pageable pageable) {
        Page<ProductResponseDto> products = hiperLibertadService.findAll(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductResponseDto>> findAll() {
        List<ProductResponseDto> products = hiperLibertadService.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDto>> findProducts(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "21") int size) {

        Page<ProductResponseDto> products = hiperLibertadService.findProducts(name, page, size);

        if (products.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-decrease-last-week")
    public ResponseEntity<Page<ProductResponseDto>> findProductsWithPriceDecreaseLastWeek(@PageableDefault(page = 0, size = 21)Pageable pageable) {
        Page<ProductResponseDto> products = hiperLibertadService.findProductsWithPriceDecreaseLastWeek(pageable);
        return ResponseEntity.ok(products);
    }
}
