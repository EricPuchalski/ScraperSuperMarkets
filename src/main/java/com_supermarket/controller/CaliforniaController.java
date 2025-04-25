package com_supermarket.controller;

import com_supermarket.service.CaliforniaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/california")
public class CaliforniaController extends BaseScraperController<CaliforniaService> {

    public CaliforniaController(CaliforniaService service) {
        super(service);
    }
}