package com_supermarket.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto {
    private Long id;
    private String name;
    private List<PriceHistoryResponseDto> priceHistory;
    private String imageUrl;
    private String productUrl;
    private String page;
    private String discountPercentage;
    private boolean hasPriceDropped;
    private boolean active;
}
