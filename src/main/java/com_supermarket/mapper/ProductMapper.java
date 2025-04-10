package com_supermarket.mapper;


import com_supermarket.dto.ProductResponseDto;
import com_supermarket.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class ProductMapper {

    @Autowired
    private PriceHistoryMapper priceHistoryMapper;

    public ProductResponseDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .productUrl(product.getProductUrl())
                .page(product.getPage())
                .priceHistory(product.getPriceHistory().stream().map(priceHistoryMapper::toDto).collect(Collectors.toList()))
                .build();
    }

    public Product toEntity(ProductResponseDto productDto) {
        if (productDto == null) {
            return null;
        }

        return Product.builder()
                .id(productDto.getId())
                .name(productDto.getName())
                .imageUrl(productDto.getImageUrl())
                .productUrl(productDto.getProductUrl())
                .page(productDto.getPage())
                .priceHistory(productDto.getPriceHistory().stream().map(priceHistoryMapper::toEntity).collect(Collectors.toList()))
                .build();
    }
}
