package com_supermarket.service;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScraperService {
    void scrapeProducts();

    List<ProductResponseDto> findProducts(String name);

    List<ProductPreviewResponseDto> findAll();

    Page<ProductPreviewResponseDto> findProducts(String name, Pageable pageable) ;

    Page<ProductPreviewResponseDto> findAll(Pageable pageable);

    Page<ProductPreviewResponseDto> findProductsWithPriceDecreaseLastWeekPageable(Pageable pageable);

    List<ProductPreviewResponseDto> findProductsWithPriceDecreaseLastWeek();

    ProductResponseDto findById(String id);
}
