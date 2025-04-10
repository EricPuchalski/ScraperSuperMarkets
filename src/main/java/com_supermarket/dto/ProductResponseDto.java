package com_supermarket.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponseDto {
    private String id;
    private String name;
    private List<PriceHistoryResponseDto> priceHistory;
    private String imageUrl;
    private String productUrl;
    private String page;
}
