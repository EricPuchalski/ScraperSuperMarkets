package com_supermarket.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class PriceHistory {
    private Double price;
    private LocalDateTime date;

    public PriceHistory() {
    }

    public PriceHistory(Double price, LocalDateTime date) {
        this.price = price;
        this.date = date;
    }


}
