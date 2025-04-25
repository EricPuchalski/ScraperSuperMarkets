package com_supermarket.mapper;


import com_supermarket.dto.ProductPreviewResponseDto;
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

        ProductResponseDto dto = ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .productUrl(product.getProductUrl())
                .page(product.getPage())
                .priceHistory(product.getPriceHistory().stream().map(priceHistoryMapper::toDto).collect(Collectors.toList()))
                .build();

        // Calculate discount if product has at least 2 price entries
        if (product.getPriceHistory().size() >= 2) {
            double previousPrice = product.getPriceHistory().get(product.getPriceHistory().size() - 2).getPrice();
            double currentPrice = product.getPriceHistory().get(product.getPriceHistory().size() - 1).getPrice();

            if (previousPrice > currentPrice) {
                double diffPercentage = ((previousPrice - currentPrice) / previousPrice) * 100;
                dto.setDiscountPercentage(String.format("%.0f%%", diffPercentage));
                dto.setHasPriceDropped(true);
            } else {
                dto.setHasPriceDropped(false);
                dto.setDiscountPercentage("0%");
            }
        } else {
            dto.setHasPriceDropped(false);
            dto.setDiscountPercentage("0%");
        }

        return dto;
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

    public ProductPreviewResponseDto toProductPreview(Product product) {
        if (product == null) {
            return null;
        }
        double currentPrice = product.getPriceHistory().get(product.getPriceHistory().size() - 1).getPrice();

        ProductPreviewResponseDto dto = ProductPreviewResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(product.getImageUrl())
                .page(product.getPage())
                .currentPrice(currentPrice)
                .build();

        // Calculate discount if product has at least 2 price entries
        if (product.getPriceHistory().size() >= 2) {
            double previousPrice = product.getPriceHistory().get(product.getPriceHistory().size() - 2).getPrice();
            dto.setPreviousPrice(previousPrice);
            if (previousPrice > currentPrice) {
                double diffPercentage = ((previousPrice - currentPrice) / previousPrice) * 100;
                dto.setDiscountPercentage(String.format("%.0f%%", diffPercentage));
                dto.setHasPriceDropped(true);
            } else {
                dto.setHasPriceDropped(false);
                dto.setDiscountPercentage("0%");
            }
        } else {
            dto.setHasPriceDropped(false);
            dto.setDiscountPercentage("0%");
        }

        return dto;
    }
}
