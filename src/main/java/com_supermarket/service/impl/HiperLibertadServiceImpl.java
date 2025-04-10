package com_supermarket.service.impl;


import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
import com_supermarket.model.PriceHistory;
import com_supermarket.model.Product;
import com_supermarket.repository.HiperLibertadRepository;
import com_supermarket.service.HiperLibertadService;
import com_supermarket.utils.ChromeUtils;
import com_supermarket.utils.HiperLibertadUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class HiperLibertadServiceImpl implements HiperLibertadService {
    private static final Logger logger = Logger.getLogger(HiperLibertadServiceImpl.class.getName());
    private static final String PAGE_NAME = "HiperLibertad";

    private final HiperLibertadRepository hiperLibertadRepository;
    private final ProductMapper productMapper;

    public HiperLibertadServiceImpl(HiperLibertadRepository hiperLibertadRepository, ProductMapper productMapper) {
        this.hiperLibertadRepository = hiperLibertadRepository;
        this.productMapper = productMapper;
    }

    @Override
    public void updateProducts() {
        ChromeOptions options = ChromeUtils.getChromeOptions();

        WebDriver driver = new ChromeDriver(options);

        List<String> categories = HiperLibertadUtils.getAllCategoryUrls();

        try {
            for (String categoryUrl : categories) {
                logger.info("Scrapeando categoria: " + categoryUrl);
                try {
                    driver.get(categoryUrl);
                    loadAllProductsByScrolling(driver);

                    List<WebElement> productElements = driver.findElements(By.cssSelector(".vtex-search-result-3-x-galleryItem"));
                    for (WebElement productElement : productElements) {
                        processProductElement(productElement);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error en URL " + categoryUrl, e);
                }
            }
        } finally {
            driver.quit();
        }

    }

    private void loadAllProductsByScrolling(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        final int[] lastProductCount = {0}; // Array para simular mutabilidad
        final int[] currentProductCount = {driver.findElements(By.cssSelector(".vtex-search-result-3-x-galleryItem")).size()};
        int unchangedCycles = 0;
        final int MAX_UNCHANGED_CYCLES = 3;

        while (unchangedCycles < MAX_UNCHANGED_CYCLES) {
            lastProductCount[0] = currentProductCount[0];
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                int newCount = driver.findElements(By.cssSelector(".vtex-search-result-3-x-galleryItem")).size();
                return newCount > lastProductCount[0] || newCount == currentProductCount[0];
            });

            currentProductCount[0] = driver.findElements(By.cssSelector(".vtex-search-result-3-x-galleryItem")).size();

            if (currentProductCount[0] == lastProductCount[0]) {
                unchangedCycles++;
            } else {
                unchangedCycles = 0;
            }
        }
    }

    private void processProductElement(WebElement productElement) {
        try {
            String title = productElement.findElement(By.cssSelector(".vtex-product-summary-2-x-productBrand")).getText();
            String productUrl = productElement.findElement(By.cssSelector(".vtex-product-summary-2-x-clearLink")).getAttribute("href");
            String imageUrl = productElement.findElement(By.cssSelector(".vtex-product-summary-2-x-imageNormal")).getAttribute("src");

            // Extracción de precio (Porque el entero está en el span 3 y el decimal en el 5)
            List<WebElement> priceSpans = productElement.findElements(By.cssSelector(".vtex-product-price-1-x-currencyContainer span"));
            String priceText = priceSpans.get(2).getText() + priceSpans.get(4).getText();
            double price = Double.parseDouble(priceText.replaceAll("[^\\d]", ""));

            Optional<Product> existingProduct = hiperLibertadRepository.findByProductUrl(productUrl);

            if (existingProduct.isPresent()) {
                updateExistingProduct(existingProduct.get(), title, imageUrl, price);
            } else {
                insertNewProduct(title, imageUrl, productUrl, price);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error procesando producto: " + productElement + "error: "+  e.getMessage(), e);
        }
    }

    private void updateExistingProduct(Product product, String title, String imageUrl, double price) {
        product.setName(title);
        product.setImageUrl(imageUrl);
        product.setPage(PAGE_NAME);

        List<PriceHistory> priceHistory = product.getPriceHistory();
        if (priceHistory.isEmpty() || price != priceHistory.get(priceHistory.size() - 1).getPrice()) {
            priceHistory.add(new PriceHistory(price, LocalDateTime.now()));
        }

        hiperLibertadRepository.save(product);
    }

    private void insertNewProduct(String title, String imageUrl, String productUrl, double price) {
        Product newProduct = Product.builder()
                .name(title)
                .imageUrl(imageUrl)
                .productUrl(productUrl)
                .page(PAGE_NAME)
                .build();
        newProduct.getPriceHistory().add(new PriceHistory(price, LocalDateTime.now()));
        hiperLibertadRepository.save(newProduct);
    }

    // Métodos auxiliares (similares a FullH4rd)
    private List<ProductResponseDto> convertToDto(List<Product> productList) {
        return productList.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> findProducts(String name) {
        List<Product> products = new ArrayList<>();
        hiperLibertadRepository.findAll().forEach(products::add); // Convertir Iterable a List

        return products.stream()
                .filter(product -> product.getName() != null &&
                        product.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(product -> PAGE_NAME.equalsIgnoreCase(product.getPage()))
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> findProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Product> allProducts = new ArrayList<>();
        hiperLibertadRepository.findAll().forEach(allProducts::add);

        // Filtrado manual
        List<Product> filteredProducts = allProducts.stream()
                .filter(product -> product.getName() != null &&
                        product.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(product -> PAGE_NAME.equalsIgnoreCase(product.getPage()))
                .collect(Collectors.toList());

        // Paginación manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());
        List<Product> pageContent = filteredProducts.subList(start, end);

        return new PageImpl<>(
                convertToDto(pageContent),
                pageable,
                filteredProducts.size()
        );
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return hiperLibertadRepository.findByPage(PAGE_NAME).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = hiperLibertadRepository.findByPage(PAGE_NAME, pageable);
        return new PageImpl<>(convertToDto(productPage.getContent()), pageable, productPage.getTotalElements());
    }
}