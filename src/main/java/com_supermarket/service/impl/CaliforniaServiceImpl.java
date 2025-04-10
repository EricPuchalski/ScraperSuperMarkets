package com_supermarket.service.impl;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.mapper.ProductMapper;
import com_supermarket.model.PriceHistory;
import com_supermarket.model.Product;
import com_supermarket.repository.CaliforniaRepository;
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
public class CaliforniaServiceImpl implements com_supermarket.service.CaliforniaService {
    private static final Logger logger = Logger.getLogger(CaliforniaServiceImpl.class.getName());
    private static final String PAGE_NAME = "California";
    private static final String BASE_URL = "https://www.californiasa.com.ar";

    private final CaliforniaRepository californiaRepository;
    private final ProductMapper productMapper;

    public CaliforniaServiceImpl(CaliforniaRepository californiaRepository, ProductMapper productMapper) {
        this.californiaRepository = californiaRepository;
        this.productMapper = productMapper;
    }

    @Override
    public void updateProducts() {
        ChromeOptions options = ChromeUtils.getChromeOptions();
        WebDriver driver = new ChromeDriver(options);

        try {
            // Empezamos con la primera página
            String currentUrl = BASE_URL + "/?post_type=product&s=&product_cat=";
            int pageNumber = 1;
            boolean hasNextPage = true;

            // Recorremos todas las páginas mientras haya un botón "siguiente"
            while (hasNextPage) {
                try {
                    logger.info("Procesando página " + pageNumber + ": " + currentUrl);
                    driver.get(currentUrl);

                    // Procesamos todos los productos de la página actual
                    List<WebElement> productElements = driver.findElements(By.cssSelector(".type-product"));
                    logger.info("Encontrados " + productElements.size() + " productos en la página " + pageNumber);

                    for (WebElement productElement : productElements) {
                        processProductElement(productElement);
                    }

                    // Verificamos si existe el enlace para ir a la siguiente página
                    List<WebElement> nextPageElements = driver.findElements(By.cssSelector(".next"));
                    if (!nextPageElements.isEmpty()) {
                        // Si hay un enlace "next", obtenemos la URL de la siguiente página
                        pageNumber++;
                        currentUrl = BASE_URL + "/page/" + pageNumber + "/?post_type=product&s=&product_cat=";
                        logger.info("Encontrado enlace a la siguiente página. Avanzando a página " + pageNumber);
                    } else {
                        // Si no hay enlace "next", terminamos el bucle
                        hasNextPage = false;
                        logger.info("No se encontró enlace a la siguiente página. Finalizando.");
                    }

                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error procesando la página " + pageNumber + ": " + e.getMessage(), e);
                    hasNextPage = false; // Si hay un error, también terminamos el bucle
                }
            }

        } finally {
            driver.quit();
        }
    }

    private void processProductElement(WebElement productElement) {
        try {
            String title = productElement.findElement(By.cssSelector(".woocommerce-loop-product__title")).getText();
            String productUrl = productElement.findElement(By.cssSelector(".woocommerce-LoopProduct-link")).getAttribute("href");
            String imageUrl = productElement.findElement(By.cssSelector(".attachment-woocommerce_thumbnail")).getAttribute("src");

            // Extracción y procesamiento del precio
            String priceText = productElement.findElement(By.cssSelector(".price")).getText();

            // Convertir el formato "19,280.00" a un número double
            // Primero eliminamos todos los caracteres que no sean dígitos, comas o puntos
            String cleanedText = priceText.replaceAll("[^\\d.,]", "");

            String finalText = cleanedText.replace(",", "");

            double price;
            try {
                price = Double.parseDouble(finalText);
            } catch (NumberFormatException e) {
                logger.warning("Error al parsear precio, intentando otro formato: " + e.getMessage());
                // Si falla, probemos otro enfoque
                if (cleanedText.contains(",") && cleanedText.contains(".")) {
                    int lastCommaIndex = cleanedText.lastIndexOf(",");
                    int lastDotIndex = cleanedText.lastIndexOf(".");

                    if (lastCommaIndex > lastDotIndex) {
                        // Formato "1.234,56" (europeo)
                        finalText = cleanedText.replace(".", "").replace(",", ".");
                    } else {
                        // Formato "1,234.56" (estadounidense)
                        finalText = cleanedText.replace(",", "");
                    }
                    logger.info("Texto del precio reformateado: '" + finalText + "'");
                    price = Double.parseDouble(finalText);
                } else {
                    // Si no podemos determinar el formato, lanzamos la excepción original
                    throw e;
                }
            }

            Optional<Product> existingProduct = californiaRepository.findByProductUrl(productUrl);

            if (existingProduct.isPresent()) {
                updateExistingProduct(existingProduct.get(), title, imageUrl, price);
            } else {
                insertNewProduct(title, imageUrl, productUrl, price);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error procesando producto: " + productElement + " error: " + e.getMessage(), e);
            logger.log(Level.INFO, "Stack trace del error:", e);
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

        californiaRepository.save(product);
    }

    private void insertNewProduct(String title, String imageUrl, String productUrl, double price) {
        Product newProduct = Product.builder()
                .name(title)
                .imageUrl(imageUrl)
                .productUrl(productUrl)
                .page(PAGE_NAME)
                .build();
        newProduct.getPriceHistory().add(new PriceHistory(price, LocalDateTime.now()));
        californiaRepository.save(newProduct);
    }

    private List<ProductResponseDto> convertToDto(List<Product> productList) {
        return productList.stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponseDto> findProducts(String name) {
        List<Product> products = new ArrayList<>();
        californiaRepository.findAll().forEach(products::add); // Convertir Iterable a List

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
        californiaRepository.findAll().forEach(allProducts::add);

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
        return californiaRepository.findByPage(PAGE_NAME).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductResponseDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = californiaRepository.findByPage(PAGE_NAME, pageable);
        return new PageImpl<>(convertToDto(productPage.getContent()), pageable, productPage.getTotalElements());
    }
}