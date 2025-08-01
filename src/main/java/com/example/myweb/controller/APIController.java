package com.example.myweb.controller;

import com.example.myweb.model.Product;
import com.example.myweb.model.User;
import com.example.myweb.service.SupabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class APIController {

    @Autowired
    private SupabaseService supabaseService;

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(defaultValue = "false") boolean isSeller) {
        boolean success = supabaseService.register(username, password, isSeller);
        return success ? "Register successful" : "Register failed";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session) {
        JsonNode user = supabaseService.login(username, password);
        if (user != null) {
            session.setAttribute("user", user); // lưu thông tin đăng nhập
            boolean isSeller = user.get("IsSeller").asBoolean();
            return isSeller ? "redirect:/seller" : "redirect:/";
        } else {
            return "redirect:/login?error";
        }
    }


    @PostMapping("/addProduct")
    public String addProduct(@RequestParam String productName,
                             @RequestParam String productDetails,
                             @RequestParam("productImage") MultipartFile imageFile,
                             @RequestParam Double productPrice,
                             @RequestParam Integer productAmount,
                             @RequestParam Long categoryID,
                             HttpSession session) {
        try {
            // 👉 Lấy userID từ session
            Long userID = (Long) session.getAttribute("userID");
            if (userID == null) {
                return "Chưa đăng nhập! Không thể thêm sản phẩm.";
            }

            String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();

            // Đường dẫn lưu ảnh trong /resources/static/uploads
            String basePath = System.getProperty("user.dir") + "/src/main/resources/static/uploads";
            File uploadDir = new File(basePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File savedFile = new File(uploadDir, fileName);
            imageFile.transferTo(savedFile);

            String imagePath = "/uploads/" + fileName;

            boolean result = supabaseService.addProduct(
                    productName,
                    productDetails,
                    imagePath,
                    productPrice,
                    productAmount,
                    categoryID,
                    userID
            );


            return result ? "Thêm sản phẩm thành công!" : "Thêm sản phẩm thất bại!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi server khi thêm sản phẩm: " + e.getMessage();
        }
    }
    @PostMapping("/getProducts")
    public ResponseEntity<List<Product>> getProducts(HttpSession session) {
        System.out.println("Gọi hàm getProducts");

        Long userID = (Long) session.getAttribute("userID");
        System.out.println("Session userID: " + userID);  // ← Thêm dòng này để debug

        if (userID == null) {
            System.out.println("userID không tồn tại trong session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Product> products = supabaseService.getProductsByUserID(Math.toIntExact(userID));
        return ResponseEntity.ok(products);
    }


}
