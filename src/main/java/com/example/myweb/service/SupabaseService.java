package com.example.myweb.service;

import com.example.myweb.model.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupabaseService {

    // 🌐 Gốc Supabase REST API
    private static final String SUPABASE_BASE_URL = "https://bszofllgootjhxncrsdn.supabase.co/rest/v1";

    // 🔑 API Key
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJzem9mbGxnb290amh4bmNyc2RuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQwNDAzMDgsImV4cCI6MjA2OTYxNjMwOH0.RJieF2OnfFcjFrUb3ZnzHhT2PKWTRDZGxKATikw97E4";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    // ✅ LOGIN
    public JsonNode login(String username, String password) {
        try {
            String url = SUPABASE_BASE_URL + "/User?UserName=eq." + username + "&UserPassword=eq." + password;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode users = objectMapper.readTree(response.body());

            if (users.isArray() && users.size() > 0) {
                return users.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ REGISTER
    public boolean register(String username, String password, boolean isSeller) {
        try {
            String checkUrl = SUPABASE_BASE_URL + "/User?UserName=eq." + username;
            HttpRequest checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create(checkUrl))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> checkResponse = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode existingUsers = objectMapper.readTree(checkResponse.body());

            if (existingUsers.isArray() && existingUsers.size() > 0) {
                return false;
            }

            String json = objectMapper.writeValueAsString(new UserRequest(username, password, isSeller));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_BASE_URL + "/User"))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ ADD PRODUCT
    public boolean addProduct(String name, String details, String image, Double price, Integer amount, Long categoryID, Long userID) {
        try {
            ObjectNode product = objectMapper.createObjectNode();
            product.put("ProductName", name);
            product.put("ProductDetails", details);
            product.put("ProductImage", image);
            product.put("ProductPrice", price);
            product.put("ProductAmount", amount);
            product.put("CategoryID", categoryID);
            product.put("UserID", userID);

            String json = objectMapper.writeValueAsString(product);
            System.out.println("Request body: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_BASE_URL + "/Product")) // 🟢 sửa đúng URL Product
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Product> getProductsByUserID(int userID) {
        String url = SUPABASE_BASE_URL + "/Product?UserID=eq." + userID;
        System.out.println("Đang gọi URL: " + url); // <-- Debug

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response code: " + response.statusCode()); // <-- Debug
            System.out.println("Response body: " + response.body());       // <-- Debug

            if (response.statusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(response.body(), new TypeReference<List<Product>>() {});
            } else {
                System.out.println("Lỗi khi tải sản phẩm từ Supabase: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    // ✅ DTO nội bộ
    static class UserRequest {
        public String UserName;
        public String UserPassword;
        public boolean IsSeller;

        public UserRequest(String userName, String userPassword, boolean isSeller) {
            this.UserName = userName;
            this.UserPassword = userPassword;
            this.IsSeller = isSeller;
        }
    }

    public class ProductRequest {
        public int ProductID;
        public int UserID;
        public String productName;
        public String productDetails;
        public String productImage;
        public Double productPrice;
        public Integer productAmount;
        public Long categoryID;
    }
}
