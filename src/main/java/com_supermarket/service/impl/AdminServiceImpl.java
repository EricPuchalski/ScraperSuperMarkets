package com_supermarket.service.impl;

import com_supermarket.repository.AdminRepository;
import com_supermarket.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;

    public AdminServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<String> findPages(){
        return adminRepository.findAllStorePages();
    }
}
