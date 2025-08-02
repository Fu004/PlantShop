package com.example.myweb.controller;

import com.example.myweb.model.*;
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
import java.time.LocalDateTime;
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
            session.setAttribute("userID", user.get("UserID").asLong());

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
        //System.out.println("Gọi hàm getProducts");

        Long userID = (Long) session.getAttribute("userID");
        System.out.println("Session userID: " + userID);  // ← Thêm dòng này để debug

        if (userID == null) {
            System.out.println("userID không tồn tại trong session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Product> products = supabaseService.getProductsByUserID(Math.toIntExact(userID));
        return ResponseEntity.ok(products);
    }

    @PostMapping("/getallProducts")
    public ResponseEntity<List<Product>> getProducts() {
        //System.out.println("Gọi hàm getProducts");

        List<Product> products = supabaseService.getProductsByUserID(Math.toIntExact(0));
        return ResponseEntity.ok(products);
    }
    @PostMapping("/addToCart")
    public ResponseEntity<String> addToCart(@RequestParam Long productId,
                                            @RequestParam Integer quantity,
                                            @RequestParam Double price, // 👈 Thêm price ở đây
                                            HttpSession session) {
        Long userID = (Long) session.getAttribute("userID");
        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().body("Số lượng không hợp lệ");
        }

        boolean success = supabaseService.addToCart(userID, productId, quantity, price); // 👈 Truyền thêm price

        if (success) {
            return ResponseEntity.ok("Đã thêm vào giỏ hàng");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm vào giỏ hàng");
        }
    }

    @GetMapping("/cartData")
    public ResponseEntity<?> getCartData(HttpSession session) {
        Long userID = (Long) session.getAttribute("userID");
        System.out.println("userID từ session: " + userID); // 👈 thêm dòng này

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        List<CartItem> cartItems = supabaseService.getCartItemsByUser(userID);
        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/deleteProduct/{productId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long productId, HttpSession session) {
        Long userID = (Long) session.getAttribute("userID");
        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        boolean success = supabaseService.deleteProductById(productId, userID);
        if (success) {
            return ResponseEntity.ok("Xóa sản phẩm thành công");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể xóa sản phẩm");
        }
    }
    @PostMapping("/updateProduct")
    public ResponseEntity<String> updateProduct(@RequestBody Product product, HttpSession session) {
        Long userID = (Long) session.getAttribute("userID");

        // 🧩 Debug 1: Kiểm tra userID trong session
        System.out.println("➡️ Session userID: " + userID);

        if (userID == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập");
        }

        product.setUserID(Math.toIntExact(userID)); // đảm bảo không bị null

        // 🧩 Debug 2: In toàn bộ thông tin sản phẩm trước khi gọi Supabase
        System.out.println("➡️ Dữ liệu sản phẩm gửi đi để cập nhật:");
        System.out.println("ProductID: " + product.getProductID());
        System.out.println("ProductName: " + product.getProductName());
        System.out.println("ProductDetails: " + product.getProductDetails());
        System.out.println("ProductImage: " + product.getProductImage());
        System.out.println("ProductPrice: " + product.getProductPrice());
        System.out.println("ProductAmount: " + product.getProductAmount());
        System.out.println("CategoryID: " + product.getCategoryID());
        System.out.println("UserID (từ session): " + product.getUserID());

        boolean success = supabaseService.editProduct(product);

        if (success) {
            return ResponseEntity.ok("Cập nhật sản phẩm thành công");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cập nhật thất bại");
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(
            @RequestBody Map<String, String> payload,
            HttpSession session
    ) {
        try {
            // Lấy userID từ session
            Object userIDObj = session.getAttribute("userID");
            if (userIDObj == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Chưa đăng nhập.");
            }
            Long userID = (Long) session.getAttribute("userID");

            // Lấy thông tin địa chỉ từ request body
            String information = payload.get("information");
            if (information == null || information.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Thiếu thông tin giao hàng.");
            }

            // Gọi SupabaseService để tạo bill
            boolean success = supabaseService.checkout(Math.toIntExact(userID), information);
            if (success) {
                return ResponseEntity.ok("Đã thanh toán thành công!");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Thanh toán thất bại.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }




}
