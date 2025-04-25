package com_supermarket.service.impl;

import com_supermarket.dto.ProductPreviewResponseDto;
import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
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
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Service
public class HiperLibertadServiceImpl extends BaseScraperService implements HiperLibertadService {

    public HiperLibertadServiceImpl(HiperLibertadRepository hiperLibertadRepository,
                                    ProductMapper productMapper) {
        super(hiperLibertadRepository, productMapper, "HiperLibertad");
    }

    // Ejecuta a las 8 PM todos los días
    @Scheduled(cron = "0 0 20 * * ?")
    public void scheduleOtroSupermercadoScraping() {
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

            // Encontrar el elemento contenedor de precio con la clase sellingPriceValue
            WebElement sellingPriceContainer = productElement.findElement(
                    By.cssSelector(".vtex-product-price-1-x-sellingPriceValue"));

            // Obtener todos los spans dentro del contenedor
            List<WebElement> priceSpans = sellingPriceContainer.findElements(By.cssSelector("span"));

            // Inicializar el texto del precio
            StringBuilder priceTextBuilder = new StringBuilder();

            // Recorrer todos los spans encontrados y concatenar sus textos
            // Asegurarse de capturar solo el primer span que contiene el precio
            if (!priceSpans.isEmpty()) {
                priceTextBuilder.append(priceSpans.get(0).getText());
            }

            String priceText = priceTextBuilder.toString();

            // Eliminar todos los caracteres que no sean dígitos o puntos
            String cleanedPriceText = priceText.replaceAll("[^\\d,]", "");

            // Reemplazar la coma decimal por un punto si es necesario
            cleanedPriceText = cleanedPriceText.replace(",", ".");

            // Convertir a double
            double price = Double.parseDouble(cleanedPriceText);

//            logger.info("El precio del producto: " + title + " es: " + price);

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

    public Page<ProductResponseDto> findAllProducts(Pageable pageable) {
        Page<Product> productPage = productRepository.findByPageAndActiveTrue(PAGE_NAME, pageable);
        return new PageImpl<>(
                convertListToDto(productPage.getContent()),
                pageable,
                productPage.getTotalElements());
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