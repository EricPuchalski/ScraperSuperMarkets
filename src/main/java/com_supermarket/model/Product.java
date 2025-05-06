package com_supermarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id")
    @Builder.Default
    @JsonIgnore // ← Esto evita que Jackson cargue la relación
    private List<PriceHistory> priceHistory = new ArrayList<>();
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "product_url")
    private String productUrl;
    private String page;
    private LocalDateTime date;
    private boolean active;
    @Column(name = "last_activation_date")
    private LocalDateTime lastActivationDate;
    @Column(name = "last_deactivation_date")
    private LocalDateTime lastDeactivationDate;

    public Double getCurrentPrice() {
        if (priceHistory.isEmpty()) return null;
        return priceHistory.get(priceHistory.size() - 1).getPrice();
    }

    public Double getPreviousPrice() {
        if (priceHistory.size() < 2) return null;
        return priceHistory.get(priceHistory.size() - 2).getPrice();
    }

    public boolean hasPriceDropped(double percentageThreshold) {
        Double current = getCurrentPrice();
        Double previous = getPreviousPrice();

        if (current == null || previous == null || previous == 0) {
            return false;
        }

        double percentageDrop = ((previous - current) / previous) * 100;
        return percentageDrop >= percentageThreshold;
    }
}