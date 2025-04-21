package com_supermarket.model;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private List<PriceHistory> priceHistory; // Historial de precios
    private String imageUrl;
    private String productUrl;
    private String page;
    private LocalDateTime date;
    private boolean active; // Indica si el producto está disponible actualmente
    private LocalDateTime lastActivationDate; // Fecha de la última vez que el producto fue activado
    private LocalDateTime lastDeactivationDate; // Fecha de la última vez que el producto fue desactivado
}
