package com.example.myweb.controller;

import com.example.myweb.model.Plant;
import com.example.myweb.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class PlantController {

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/cart")
    public String showCart() {
        return "cart";
    }

    @GetMapping("/invoice")
    public String products() {
        return "invoice";
    }
    @GetMapping("/seller")
    public String sellerPage(HttpSession session, Model model) {
        User user =  (User) session.getAttribute("UserDefined");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
       // model.addAttribute("userID", us);
        return "seller";
    }

    @GetMapping("/")
    public String showIndex(HttpSession session, Model model) {
        User user =  (User) session.getAttribute("UserDefined");

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "index";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }



}
