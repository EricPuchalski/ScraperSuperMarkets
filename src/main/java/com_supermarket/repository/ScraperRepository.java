package com_supermarket.repository;

import com_supermarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ScraperRepository extends CrudRepository<Product, String> {
    Optional<Product> findByProductUrl(String productUrl);
    Page<Product> findByPage(String pageName, Pageable pageable);
    List<Product> findByPage(String pageName);
    List<Product> findByName(String name);
    // Métodos para búsqueda por nombre ignorando mayúsculas/minúsculas
    List<Product> findByPageAndNameContainingIgnoreCase(String page, String name);
    Page<Product> findByPageAndNameContainingIgnoreCase(String page, String name, Pageable pageable);

    //Metodos para deshabilitar y habilitar
    Page<Product> findByPageAndActiveTrue(String page, Pageable pageable);
    List<Product> findByPageAndActiveTrue(String page);

    @Query(value = """
    WITH ranked_prices AS (
        SELECT 
            ph.product_id,
            ph.price,
            ph.date,
            ROW_NUMBER() OVER (PARTITION BY ph.product_id ORDER BY ph.date DESC) as rn
        FROM price_history ph
    )
    SELECT p.* 
    FROM products p
    JOIN ranked_prices r1 ON p.id = r1.product_id AND r1.rn = 1
    JOIN ranked_prices r2 ON p.id = r2.product_id AND r2.rn = 2
    WHERE p.page = :pageName 
    AND p.active = true
    AND r1.price < r2.price
    AND ((r2.price - r1.price) / r2.price * 100) >= :percentageDrop
""", nativeQuery = true, countQuery = """
    SELECT COUNT(*)
    FROM (
        WITH ranked_prices AS (
            SELECT 
                ph.product_id,
                ph.price,
                ROW_NUMBER() OVER (PARTITION BY ph.product_id ORDER BY ph.date DESC) as rn
            FROM price_history ph
        )
        SELECT p.id
        FROM products p
        JOIN ranked_prices r1 ON p.id = r1.product_id AND r1.rn = 1
        JOIN ranked_prices r2 ON p.id = r2.product_id AND r2.rn = 2
        WHERE p.page = :pageName 
        AND p.active = true
        AND r1.price < r2.price
        AND ((r2.price - r1.price) / r2.price * 100) >= :percentageDrop
    ) as count_query
""")
    Page<Product> findProductsWithPriceDecrease(
            @Param("pageName") String pageName,
            @Param("percentageDrop") double percentageDrop,
            Pageable pageable);


}
