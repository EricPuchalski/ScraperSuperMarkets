package com_supermarket.service.impl;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
import com_supermarket.model.Product;
import com_supermarket.repository.CaliforniaRepository;
import com_supermarket.service.CaliforniaService;
import com_supermarket.utils.ChromeUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class CaliforniaServiceImpl extends BaseScraperService implements CaliforniaService {
    private static final String PAGE_NAME = "California";
    private static final String BASE_URL = "https://www.californiasa.com.ar";

    public CaliforniaServiceImpl(CaliforniaRepository californiaRepository,
                                 ProductMapper productMapper) {
        super(californiaRepository, productMapper, PAGE_NAME);
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
        String currentUrl = BASE_URL + "/?post_type=product&s=&product_cat=";
        int pageNumber = 1;
        boolean hasNextPage = true;

        while (hasNextPage) {
            try {
                logger.info("Procesando página " + pageNumber + ": " + currentUrl);
                driver.get(currentUrl);

                List<WebElement> productElements = driver.findElements(By.cssSelector(".type-product"));
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
                    currentUrl = BASE_URL + "/page/" + pageNumber + "/?post_type=product&s=&product_cat=";
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
            String title = productElement.findElement(By.cssSelector(".woocommerce-loop-product__title")).getText();
            String productUrl = productElement.findElement(By.cssSelector(".woocommerce-LoopProduct-link")).getAttribute("href");
            String imageUrl = productElement.findElement(By.cssSelector(".attachment-woocommerce_thumbnail")).getAttribute("src");
            String priceText = productElement.findElement(By.cssSelector(".price")).getText();

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
        Page<Product> productPage = productRepository.findByPageAndActiveTrue(PAGE_NAME, pageable);
        return new PageImpl<>(
                convertListToProductPreview(productPage.getContent()),
                pageable,
                productPage.getTotalElements());
    }

    @Override
    public Page<ProductPreviewResponseDto> findProductsWithPriceDecreaseLastWeekPageable(Pageable pageable) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 1. Primero obtener TODOS los productos activos
        List<Product> allActiveProducts = productRepository.findByPageAndActiveTrue(PAGE_NAME);

        // 2. Filtrar los que tuvieron disminución
        List<Product> productsWithDecrease = allActiveProducts.stream()
                .filter(product -> hasPriceDecreasedInLastWeek(product, sevenDaysAgo))
                .collect(Collectors.toList());

        return new PageImpl<>(
                convertListToProductPreview(productsWithDecrease),
                pageable,
                productsWithDecrease.size());
    }

    @Override
    public List<ProductPreviewResponseDto> findProductsWithPriceDecreaseLastWeek() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 1. Primero obtener TODOS los productos activos
        List<Product> allActiveProducts = productRepository.findByPageAndActiveTrue(PAGE_NAME);

        // 2. Filtrar los que tuvieron disminución
        List<Product> productsWithDecrease = allActiveProducts.stream()
                .filter(product -> hasPriceDecreasedInLastWeek(product, sevenDaysAgo))
                .collect(Collectors.toList());

        return convertListToProductPreview(productsWithDecrease);
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