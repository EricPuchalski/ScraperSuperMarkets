package com_supermarket.service;

import com_supermarket.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScraperService {
    void scrapeProducts();

    List<ProductResponseDto> findProducts(String name);

    List<ProductResponseDto> findAll();

    Page<ProductResponseDto> findProducts(String name, int page, int size) ;

    Page<ProductResponseDto> findAll(Pageable pageable);

    Page<ProductResponseDto> findProductsWithPriceDecreaseLastWeek(Pageable pageable);
}
