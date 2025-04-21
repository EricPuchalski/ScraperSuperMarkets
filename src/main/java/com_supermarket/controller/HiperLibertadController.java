package com_supermarket.controller;


import com_supermarket.service.HiperLibertadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hiperlibertad")
public class HiperLibertadController extends BaseScraperController<HiperLibertadService> {

    public HiperLibertadController(HiperLibertadService service) {
        super(service);
    }
}