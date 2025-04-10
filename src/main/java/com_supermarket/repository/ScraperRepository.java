package com_supermarket.repository;

import com_supermarket.dto.ProductResponseDto;
import com_supermarket.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

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

    // Método para guardar todos los productos en batch
    <S extends Product> Iterable<S> saveAll(Iterable<S> entities);

}
