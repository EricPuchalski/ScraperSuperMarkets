package com_supermarket.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminRepository extends ScraperRepository{
    @Query("SELECT DISTINCT p.page FROM Product p")
    List<String> findAllStorePages();
}
