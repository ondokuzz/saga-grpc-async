package com.demirsoft.controller.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class Controller {

    @GetMapping("path")
    public String getInventory(@RequestParam String param) {
        return new String();
    }
    
    
}
