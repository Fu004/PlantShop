package com.example.myweb.model;

import jakarta.persistence.*;

@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartID;

    @ManyToOne
    @JoinColumn(name = "userID")
    private User user;

    // Getters & Setters
}
