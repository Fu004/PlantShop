package com.example.myweb.controller;

import com.example.myweb.model.Plant;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class PlantController {

    @GetMapping("/")
    public String showPlants(Model model) {
        List<Plant> plants = Arrays.asList(
                new Plant("Cây Sen Đá", "/images/Sen_da.jpg", 50000),
                new Plant("Cây Xương Rồng", "/images/catus.jpg", 70000),
                new Plant("Cây Kim Tiền", "images/Kim_tien.jpg", 120000)
        );
        model.addAttribute("plants", plants);
        return "index";
    }
}
