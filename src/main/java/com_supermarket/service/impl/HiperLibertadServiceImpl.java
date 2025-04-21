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
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class HiperLibertadServiceImpl extends BaseSupermarketService implements HiperLibertadService {

    public HiperLibertadServiceImpl(HiperLibertadRepository hiperLibertadRepository,
                                    ProductMapper productMapper) {
        super(hiperLibertadRepository, productMapper, "HiperLibertad");
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
        List<String> categories = HiperLibertadUtils.getAllCategoryUrls();

        for (String categoryUrl : categories) {
            logger.info("Scrapeando categoria: " + categoryUrl);
            try {
                driver.get(categoryUrl);
                scrollToLoadAllProducts(driver);

                List<WebElement> productElements = driver.findElements(
                        By.cssSelector(".vtex-search-result-3-x-galleryItem"));

                for (WebElement productElement : productElements) {
                    String productUrl = processProductElement(productElement);
                    if (productUrl != null) {
                        foundProductUrls.add(productUrl);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error en URL " + categoryUrl, e);
            }
        }
    }

    @Override
    protected String processProductElement(WebElement productElement) {
        try {
            String title = productElement.findElement(
                    By.cssSelector(".vtex-product-summary-2-x-productBrand")).getText();
            String productUrl = productElement.findElement(
                    By.cssSelector(".vtex-product-summary-2-x-clearLink")).getAttribute("href");
            String imageUrl = productElement.findElement(
                    By.cssSelector(".vtex-product-summary-2-x-imageNormal")).getAttribute("src");

            List<WebElement> priceSpans = productElement.findElements(
                    By.cssSelector(".vtex-product-price-1-x-currencyContainer span"));
            String priceText = priceSpans.get(2).getText() + priceSpans.get(4).getText();
            double price = Double.parseDouble(priceText.replaceAll("[^\\d]", ""));

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

    private void scrollToLoadAllProducts(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        final int[] lastProductCount = {0};
        final int[] currentProductCount = {driver.findElements(
                By.cssSelector(".vtex-search-result-3-x-galleryItem")).size()};
        int unchangedCycles = 0;
        final int MAX_UNCHANGED_CYCLES = 3;

        while (unchangedCycles < MAX_UNCHANGED_CYCLES) {
            lastProductCount[0] = currentProductCount[0];
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");

            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
                int newCount = driver.findElements(
                        By.cssSelector(".vtex-search-result-3-x-galleryItem")).size();
                return newCount > lastProductCount[0] || newCount == currentProductCount[0];
            });

            currentProductCount[0] = driver.findElements(
                    By.cssSelector(".vtex-search-result-3-x-galleryItem")).size();

            if (currentProductCount[0] == lastProductCount[0]) {
                unchangedCycles++;
            } else {
                unchangedCycles = 0;
            }
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
    public Page<ProductResponseDto> findProducts(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findByPageAndNameContainingIgnoreCase(
                PAGE_NAME, name, pageable);
        return new PageImpl<>(
                convertListToDto(productPage.getContent()),
                pageable,
                productPage.getTotalElements());
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findByPageAndActiveTrue(PAGE_NAME).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> findAll(Pageable pageable) {
        Page<Product> productPage = productRepository.findByPageAndActiveTrue(PAGE_NAME, pageable);
        return new PageImpl<>(
                convertListToDto(productPage.getContent()),
                pageable,
                productPage.getTotalElements());
    }

    @Override
    public Page<ProductResponseDto> findProductsWithPriceDecreaseLastWeek(Pageable pageable) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Page<Product> activeProducts = productRepository.findByPageAndActiveTrue(PAGE_NAME, pageable);

        List<Product> productsWithDecrease = activeProducts.getContent().stream()
                .filter(product -> hasPriceDecreasedInLastWeek(product, sevenDaysAgo))
                .collect(Collectors.toList());

        return new PageImpl<>(
                convertListToDto(productsWithDecrease),
                pageable,
                productsWithDecrease.size());
    }

}