package com_supermarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    @Builder.Default
    private List<PriceHistory> priceHistory = new ArrayList<>(); // Inicializado con @Builder.Default
    private String imageUrl;
    private String productUrl;
    private String page;
    private LocalDateTime date;
    private boolean active;
    private LocalDateTime lastActivationDate;
    private LocalDateTime lastDeactivationDate;

    // Constructor conveniente para uso común
    public Product(String title, String imageUrl, String productUrl, String page) {
        this.name = title;
        this.imageUrl = imageUrl;
        this.productUrl = productUrl;
        this.page = page;
        this.priceHistory = new ArrayList<>();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PriceHistory> getPriceHistory() {
        return priceHistory;
    }

    public void setPriceHistory(List<PriceHistory> priceHistory) {
        this.priceHistory = priceHistory != null ? priceHistory : new ArrayList<>();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastActivationDate() {
        return lastActivationDate;
    }

    public void setLastActivationDate(LocalDateTime lastActivationDate) {
        this.lastActivationDate = lastActivationDate;
    }

    public LocalDateTime getLastDeactivationDate() {
        return lastDeactivationDate;
    }

    public void setLastDeactivationDate(LocalDateTime lastDeactivationDate) {
        this.lastDeactivationDate = lastDeactivationDate;
    }
}