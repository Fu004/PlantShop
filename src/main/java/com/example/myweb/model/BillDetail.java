package com.example.myweb.model;

import jakarta.persistence.*;

@Entity
public class BillDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billDetailID;

    @ManyToOne
    @JoinColumn(name = "billID")
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "productID")
    private Product product;

    private Integer quantity;
    private Double price;

    // Getters & Setters
}
