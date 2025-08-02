package com.example.myweb.model;

public class BillDetail {
    private Long billDetailID;   // Optional nếu Supabase tự sinh
    private Long billID;
    private Long productID;
    private Integer quantity;
    private Double price;

    // Constructors
    public BillDetail() {}

    public BillDetail(Long billID, Long productID, Integer quantity, Double price) {
        this.billID = billID;
        this.productID = productID;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters
    public Long getBillDetailID() {
        return billDetailID;
    }

    public void setBillDetailID(Long billDetailID) {
        this.billDetailID = billDetailID;
    }

    public Long getBillID() {
        return billID;
    }

    public void setBillID(Long billID) {
        this.billID = billID;
    }

    public Long getProductID() {
        return productID;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
