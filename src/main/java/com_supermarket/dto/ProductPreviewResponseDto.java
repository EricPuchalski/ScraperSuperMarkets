package com_supermarket.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductPreviewResponseDto {
    private Long id;
    private String name;
    private String imageUrl;
    private String page;
    private Double currentPrice;
    private Double previousPrice;
    private String discountPercentage;
    private boolean hasPriceDropped;
    private boolean active;
}
