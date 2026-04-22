package com_supermarket.service.impl;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
import com_supermarket.model.PriceHistory;
import com_supermarket.model.Product;
import com_supermarket.repository.CaliforniaRepository;
import com_supermarket.service.CaliforniaService;
import com_supermarket.utils.ChromeUtils;
import jakarta.transaction.Transactional;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
@Transactional
public class CaliforniaServiceImpl extends BaseScraperService implements CaliforniaService {
    private static final String PAGE_NAME = "California";
    private static final String BASE_URL = "https://www.californiasa.com.ar/productos";

    public CaliforniaServiceImpl(CaliforniaRepository californiaRepository,
                                 ProductMapper productMapper) {
        super(californiaRepository, productMapper, PAGE_NAME);
    }

    // Ejecuta a las 7 PM todos los días
    @Scheduled(cron = "0 0 19 * * ?")
    public void scheduleHiperLibertadScraping() {
        this.scrapeProducts();
    }

    @Override
    public void scrapeProducts() {
        ChromeOptions options = ChromeUtils.getChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        Set<String> foundProductUrls = new HashSet<>();

        try {
            loadAllProducts(driver, foundProductUrls);
            updateProductAvailability(foundProductUrls);
        } finally {
            driver.quit();
        }
    }

    @Override
    protected void loadAllProducts(WebDriver driver, Set<String> foundProductUrls) {
        String currentUrl = BASE_URL + "?product_page=";
        int pageNumber = 1;
        boolean hasNextPage = true;

        while (hasNextPage) {
            try {
                logger.info("Procesando página " + pageNumber + ": " + currentUrl);
                driver.get(currentUrl);

                List<WebElement> productElements = driver.findElements(By.cssSelector(".csc-product-card"));
                logger.info("Encontrados " + productElements.size() + " productos en la página " + pageNumber);

                for (WebElement productElement : productElements) {
                    String productUrl = processProductElement(productElement);
                    if (productUrl != null) {
                        foundProductUrls.add(productUrl);
                    }
                }

                List<WebElement> nextPageElements = driver.findElements(By.cssSelector(".next"));
                if (!nextPageElements.isEmpty()) {
                    pageNumber++;
                    currentUrl = BASE_URL + "?product_page=" + pageNumber;
                    logger.info("Avanzando a página " + pageNumber);
                } else {
                    hasNextPage = false;
                    logger.info("No se encontró enlace a la siguiente página. Finalizando.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error procesando la página " + pageNumber, e);
                hasNextPage = false;
            }
        }
    }

    @Override
    protected String processProductElement(WebElement productElement) {
        try {
            String title = productElement
                    .findElement(By.cssSelector(".csc-product-title a"))
                    .getText();            String productUrl = productElement.findElement(By.cssSelector(".csc-product-image-link")).getAttribute("href");
            String imageUrl = productElement.findElement(By.cssSelector(".attachment-woocommerce_thumbnail")).getAttribute("src");
            String priceText = productElement
                    .findElement(By.cssSelector(".csc-product-price .woocommerce-Price-amount"))
                    .getText();


            double price = parsePrice(priceText);

            Optional<Product> existingProduct = productRepository.findByProductUrl(productUrl);

            if (existingProduct.isPresent()) {
                updateExistingProduct(existingProduct.get(), title, imageUrl, price);
            } else {
                insertNewProduct(title, imageUrl, productUrl, price);
            }

            return productUrl;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error procesando producto: " + e.getMessage(), e);
            return null;
        }
    }

    private double parsePrice(String priceText) {
        String cleanedText = priceText.replaceAll("[^\\d.,]", "");
        String finalText = cleanedText.replace(",", "");

        try {
            return Double.parseDouble(finalText);
        } catch (NumberFormatException e) {
            logger.warning("Error al parsear precio, intentando otro formato: " + e.getMessage());

            if (cleanedText.contains(",") && cleanedText.contains(".")) {
                int lastCommaIndex = cleanedText.lastIndexOf(",");
                int lastDotIndex = cleanedText.lastIndexOf(".");

                if (lastCommaIndex > lastDotIndex) {
                    finalText = cleanedText.replace(".", "").replace(",", ".");
                } else {
                    finalText = cleanedText.replace(",", "");
                }
                logger.info("Texto del precio reformateado: '" + finalText + "'");
                return Double.parseDouble(finalText);
            }
            throw e;
        }
    }

    @Override
    public List<ProductResponseDto> findProducts(String name) {
        return productRepository.findByPageAndNameContainingIgnoreCase(PAGE_NAME, name).stream()
                .filter(Product::isActive)
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductPreviewResponseDto> findProducts(String name, Pageable pageable) {
        Page<Product> productPage = productRepository.findByPageAndNameContainingIgnoreCase(
                PAGE_NAME, name, pageable);
        return new PageImpl<>(
                convertListToProductPreview(productPage.getContent()),
                pageable,
                productPage.getTotalElements());
    }

    @Override
    public List<ProductPreviewResponseDto> findAll() {
        return productRepository.findByPageAndActiveTrue(PAGE_NAME).stream()
                .map(productMapper::toProductPreview)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductPreviewResponseDto> findAll(Pageable pageable) {
        return productRepository.findByPageAndActiveTrue(PAGE_NAME, pageable)
                .map(productMapper::toProductPreview);
    }
    @Override
    public Page<ProductPreviewResponseDto> findProductsWithPriceDecreasePageable(Pageable pageable) {
        final double percentageThreshold = 5.0;

        Page<Product> productsPage = productRepository.findProductsWithPriceDecrease(
                PAGE_NAME,
                percentageThreshold,
                pageable
        );

        return productsPage.map(product -> {
            ProductPreviewResponseDto dto = productMapper.toProductPreview(product);
            // Como el repositorio ya filtró por descuento actual, podemos asegurar que hay descuento
            Double current = product.getCurrentPrice();
            Double previous = product.getPreviousPrice();
            double discount = ((previous - current) / previous) * 100;
            dto.setDiscountPercentage(String.valueOf((int) Math.round(discount)));
            return dto;
        });
    }



    @Override
    public ProductResponseDto findById(String id) {
        Product findProduct =  productRepository.findById(id).orElse(null);
        if (findProduct != null) {
            return productMapper.toDto(findProduct);
        } else {
            return null;
        }
    }
}