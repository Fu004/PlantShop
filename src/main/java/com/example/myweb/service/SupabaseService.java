package com.example.myweb.service;

import com.example.myweb.model.Bill;
import com.example.myweb.model.BillDetail;
import com.example.myweb.model.CartItem;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
           // System.out.println("Request body: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_BASE_URL + "/Product")) // 🟢 sửa đúng URL Product
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        //    System.out.println("Response code: " + response.statusCode());
         //   System.out.println("Response body: " + response.body());

            return response.statusCode() == 201 || response.statusCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<Product> getProductsByUserID(int userID) {
        String url;
        if (userID == 0) {
            // Nếu userID là 0, lấy toàn bộ sản phẩm
            url = SUPABASE_BASE_URL + "/Product";
        } else {
            // Nếu khác 0, lọc theo UserID
            url = SUPABASE_BASE_URL + "/Product?UserID=eq." + userID;
        }

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
    public boolean addToCart(Long userID, Long productId, Integer quantity, Double price) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper objectMapper = new ObjectMapper();

            // 1. Kiểm tra xem user đã có cart chưa
            String cartCheckUrl = SUPABASE_BASE_URL + "/Cart?UserID=eq." + userID;
            HttpRequest cartCheckRequest = HttpRequest.newBuilder()
                    .uri(URI.create(cartCheckUrl))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> cartCheckResponse = client.send(cartCheckRequest, HttpResponse.BodyHandlers.ofString());

            if (cartCheckResponse.statusCode() != 200) {
                System.err.println("❌ Lỗi khi kiểm tra Cart: " + cartCheckResponse.body());
                return false;
            }

            JsonNode cartArray = objectMapper.readTree(cartCheckResponse.body());
            Long cartID;

            // 2. Nếu chưa có Cart, tạo mới
            if (cartArray.isEmpty()) {
                ObjectNode newCart = objectMapper.createObjectNode();
                newCart.put("UserID", userID);

                HttpRequest createCartRequest = HttpRequest.newBuilder()
                        .uri(URI.create(SUPABASE_BASE_URL + "/Cart"))
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(newCart.toString()))
                        .build();

                HttpResponse<String> createCartResponse = client.send(createCartRequest, HttpResponse.BodyHandlers.ofString());

                if (createCartResponse.statusCode() != 201) {
                    System.err.println("❌ Lỗi khi tạo Cart mới: " + createCartResponse.body());
                    return false;
                }

                JsonNode createdCart = objectMapper.readTree(createCartResponse.body());
                cartID = createdCart.get(0).get("CartID").asLong();
            } else {
                cartID = cartArray.get(0).get("CartID").asLong();
            }

            // 3. Thêm sản phẩm vào giỏ hàng
            ObjectNode cartItem = objectMapper.createObjectNode();
            cartItem.put("CartID", cartID);
            cartItem.put("ProductID", productId);
            cartItem.put("Quantity", quantity);
            cartItem.put("Price", price); // 👈 THÊM GIÁ

            HttpRequest addItemRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_BASE_URL + "/cartitem"))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(cartItem.toString()))
                    .build();

            HttpResponse<String> addItemResponse = client.send(addItemRequest, HttpResponse.BodyHandlers.ofString());

            if (addItemResponse.statusCode() == 201) {
                return true;
            } else {
                System.err.println("❌ Lỗi khi thêm sản phẩm vào giỏ hàng: " + addItemResponse.body());
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<CartItem> getCartItemsByUser(Long userID) {
        List<CartItem> cartItems = new ArrayList<>();

        try {
            System.out.println("Bắt đầu lấy CartID cho UserID: " + userID);

            // Bước 1: Lấy cartID dựa trên userID
            String cartUrl = SUPABASE_BASE_URL + "/Cart?UserID=eq." + userID;
            System.out.println("Cart URL: " + cartUrl);

            HttpRequest cartRequest = HttpRequest.newBuilder()
                    .uri(URI.create(cartUrl))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> cartResponse = client.send(cartRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Cart response: " + cartResponse.body());

            JsonNode cartArray = objectMapper.readTree(cartResponse.body());

            if (!cartArray.isArray() || cartArray.size() == 0) {
                System.out.println("Không tìm thấy giỏ hàng nào cho userID: " + userID);
                return cartItems;
            }

            Long cartID = cartArray.get(0).get("CartID").asLong();
            System.out.println("Tìm thấy CartID: " + cartID);

            // Bước 2: Lấy các cart item và join với Product
            String itemsUrl = SUPABASE_BASE_URL +
                    "/cartitem?CartID=eq." + cartID +
                    "&select=CartItemID,Quantity,Price,ProductID,Product(ProductID,ProductName,ProductImage,ProductPrice)";

            System.out.println("Items URL: " + itemsUrl);

            HttpRequest itemsRequest = HttpRequest.newBuilder()
                    .uri(URI.create(itemsUrl))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> itemsResponse = client.send(itemsRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Items response: " + itemsResponse.body());

            JsonNode itemsArray = objectMapper.readTree(itemsResponse.body());

            for (JsonNode itemNode : itemsArray) {
                CartItem cartItem = new CartItem();
                cartItem.setCartItemID((int) itemNode.get("CartItemID").asLong());
                cartItem.setQuantity(itemNode.get("Quantity").asInt());
                cartItem.setPrice((int) itemNode.get("Price").asDouble());

                JsonNode productNode = itemNode.get("Product");
                if (productNode != null && !productNode.isNull()) {
                    Product product = new Product();
                    product.setProductID(productNode.get("ProductID").asLong());
                    product.setProductName(productNode.get("ProductName").asText());
                    product.setProductImage(productNode.get("ProductImage").asText());
                    product.setProductPrice(productNode.get("ProductPrice").asDouble());
                    cartItem.setProduct(product);
                } else {
                    System.out.println("Không có thông tin sản phẩm cho CartItemID: " + cartItem.getCartItemID());
                }

                cartItems.add(cartItem);
            }

            System.out.println("Tổng số cart item lấy được: " + cartItems.size());

        } catch (Exception e) {
            System.out.println("Lỗi xảy ra khi lấy cart items: ");
            e.printStackTrace();
        }

        return cartItems;
    }

    public boolean deleteProductById(Long productId, Long userId) {
        try {
            String url = SUPABASE_BASE_URL + "/Product?ProductID=eq." + productId + "&UserID=eq." + userId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("DELETE", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("DELETE response code: " + response.statusCode());
            System.out.println("DELETE response body: " + response.body());

            return response.statusCode() == 204;
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi gọi Supabase để xóa sản phẩm:");
            e.printStackTrace();
            return false;
        }
    }
    public boolean editProduct(Product product) {
        try {
            String url = SUPABASE_BASE_URL + "/Product?ProductID=eq." + product.getProductID();


            // Dữ liệu cần cập nhật
            ObjectNode updatedFields = objectMapper.createObjectNode();
            updatedFields.put("ProductName", product.getProductName());
            updatedFields.put("ProductDetails", product.getProductDetails());
            updatedFields.put("ProductPrice", product.getProductPrice());
            updatedFields.put("ProductAmount", product.getProductAmount());
            updatedFields.put("ProductImage", product.getProductImage()); // Nếu cần sửa ảnh
            updatedFields.put("UserID", product.getUserID());

            String requestBody = objectMapper.writeValueAsString(updatedFields);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", SUPABASE_API_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "return=minimal")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("PATCH response code: " + response.statusCode());
            System.out.println("PATCH response body: " + response.body());

            return response.statusCode() == 204;

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi cập nhật sản phẩm:");
            e.printStackTrace();
            return false;
        }
    }

    public int createBill(Bill bill) throws IOException, InterruptedException {
        ObjectNode billNode = objectMapper.createObjectNode();
        billNode.put("UserID", bill.getUserID());
        billNode.put("CreatedAt", bill.getCreatedAt().toString()); // yyyy-MM-dd
        billNode.put("Status", 0); // Giả sử mặc định là 0
        billNode.put("Information", bill.getInformation());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_BASE_URL + "/Bill"))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(billNode.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            JsonNode json = objectMapper.readTree(response.body());
            return json.get(0).get("BillID").asInt();
        }

        return -1;
    }

    public boolean createBillDetail(BillDetail detail) throws IOException, InterruptedException {
        ObjectNode detailNode = objectMapper.createObjectNode();
        detailNode.put("BillID", detail.getBillID());
        detailNode.put("ProductID", detail.getProductID());
        detailNode.put("Quantity", detail.getQuantity());
        detailNode.put("Price", detail.getPrice());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_BASE_URL + "/BillDetail"))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(detailNode.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 201;
    }
    public void clearCart(int userID) throws IOException, InterruptedException {
        int cartID = getCartIDByUserID(userID);  // ✅ đã sửa tên hàm
        if (cartID == -1) {
            System.out.println("⚠️ [clearCart] Không tìm thấy CartID để xoá.");
            return;
        }

        String url = SUPABASE_BASE_URL + "/cartitem?CartID=eq." + cartID;
        System.out.println("🧹 [clearCart] URL: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("🧹 [clearCart] Status Code: " + response.statusCode());
        System.out.println("🧹 [clearCart] Response Body: " + response.body());
    }



    public boolean checkout(int userID, String information) throws IOException, InterruptedException {
        List<CartItem> items = getCartItemsByUser((long) userID);
        if (items.isEmpty()) return false;

        Bill bill = new Bill();
        bill.setUserID(userID);
        bill.setCreatedAt(LocalDate.now().atStartOfDay());
        bill.setInformation(information);
        int billID = createBill(bill);
        if (billID <= 0) return false;

        for (CartItem item : items) {
            BillDetail detail = new BillDetail();
            detail.setBillID((long) billID);
            detail.setProductID(item.getProduct().getProductID());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getProduct().getProductPrice());
            createBillDetail(detail);
        }

        // ✅ Sửa chỗ này
        clearCart(userID);
        return true;
    }

    public int getCartIDByUserID(int userID) throws IOException, InterruptedException {
        String url = SUPABASE_BASE_URL + "/Cart?UserID=eq." + userID + "&select=CartID";
        System.out.println("🔍 [getCartIDByUserID] URL: " + url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SUPABASE_API_KEY)
                .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("🔍 [getCartIDByUserID] Status Code: " + response.statusCode());
        System.out.println("🔍 [getCartIDByUserID] Response Body: " + response.body());

        if (response.statusCode() == 200) {
            JsonNode json = objectMapper.readTree(response.body());
            if (json.isArray() && json.size() > 0) {
                return json.get(0).get("CartID").asInt();
            } else {
                System.out.println("⚠️ [getCartIDByUserID] Không tìm thấy cart cho userID = " + userID);
            }
        } else {
            System.out.println("❌ [getCartIDByUserID] Lỗi gọi Supabase API.");
        }

        return -1;
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
