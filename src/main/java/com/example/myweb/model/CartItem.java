package com.example.myweb.model;

import jakarta.persistence.*;

@Entity
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemID;

    @ManyToOne
    @JoinColumn(name = "cartID")
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productID")
    private Product product;

    private Integer quantity;
    private Double price;

    // Getters & Setters
}
