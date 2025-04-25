package com_supermarket.service.impl;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
import com_supermarket.model.PriceHistory;
import com_supermarket.model.Product;
import com_supermarket.repository.ScraperRepository;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public abstract class BaseScraperService {
    protected final Logger logger;
    protected final ScraperRepository productRepository;
    protected final ProductMapper productMapper;
    protected final String PAGE_NAME;

    public BaseScraperService(ScraperRepository productRepository,
                              ProductMapper productMapper,
                              String pageName) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.PAGE_NAME = pageName;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    // Métodos implementados (comunes)
    protected void updateProductAvailability(Set<String> foundProductUrls) {
        List<Product> allProducts = productRepository.findByPage(PAGE_NAME);

        for (Product product : allProducts) {
            boolean wasFound = foundProductUrls.contains(product.getProductUrl());
            if (!wasFound && product.isActive()) {
                product.setActive(false);
                product.setLastDeactivationDate(LocalDateTime.now());
            } else if (wasFound && !product.isActive()) {
                product.setActive(true);
                product.setLastActivationDate(LocalDateTime.now());
            }
            productRepository.save(product);
        }
    }

    protected void updateExistingProduct(Product product, String title, String imageUrl, double price) {
        product.setName(title);
        product.setImageUrl(imageUrl);

        if (!product.isActive()) {
            product.setActive(true);
            product.setLastActivationDate(LocalDateTime.now());
        }

        if (product.getPriceHistory().isEmpty() ||
                price != product.getPriceHistory().get(product.getPriceHistory().size() - 1).getPrice()) {
            product.getPriceHistory().add(new PriceHistory(price, LocalDateTime.now()));
        }

        productRepository.save(product);
    }

    protected void insertNewProduct(String title, String imageUrl, String productUrl, double price) {
        Product newProduct = Product.builder()
                .name(title)
                .imageUrl(imageUrl)
                .productUrl(productUrl)
                .page(PAGE_NAME)
                .active(true)
                .date(LocalDateTime.now())
                .lastActivationDate(LocalDateTime.now())
                .build();
        newProduct.getPriceHistory().add(new PriceHistory(price, LocalDateTime.now()));
        productRepository.save(newProduct);
    }

    // En BaseSupermarketService.java
    protected List<ProductResponseDto> convertListToDto(List<Product> productList) {
        return productList.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    protected List<ProductPreviewResponseDto> convertListToProductPreview(List<Product> productList) {
        return productList.stream()
                .map(productMapper::toProductPreview)
                .collect(Collectors.toList());
    }

    protected boolean hasPriceDecreasedInLastWeek(Product product, LocalDateTime sevenDaysAgo) {
        if (product.getPriceHistory() == null || product.getPriceHistory().isEmpty()) {
            return false;
        }

        List<PriceHistory> sortedHistory = product.getPriceHistory().stream()
                .sorted(Comparator.comparing(PriceHistory::getDate).reversed())
                .toList();

        // Precio más reciente dentro de la semana
        Optional<PriceHistory> currentPrice = sortedHistory.stream()
                .filter(ph -> ph.getDate().isAfter(sevenDaysAgo))
                .findFirst();

        if (currentPrice.isEmpty()) {
            return false;
        }

        // Buscar el precio de referencia (el más reciente ANTES de la semana)
        Optional<PriceHistory> referencePrice = sortedHistory.stream()
                .filter(ph -> ph.getDate().isBefore(sevenDaysAgo))
                .findFirst();

        // Si no hay precio de referencia, no podemos comparar
        return referencePrice.filter(priceHistory -> sortedHistory.stream()
                .filter(ph -> ph.getDate().isAfter(sevenDaysAgo))
                .anyMatch(ph -> ph.getPrice() < priceHistory.getPrice())).isPresent();

    }

    // Métodos abstractos (implementación obligatoria)
    protected abstract String processProductElement(WebElement productElement);
    protected abstract void loadAllProducts(WebDriver driver, Set<String> foundProductUrls);
}
