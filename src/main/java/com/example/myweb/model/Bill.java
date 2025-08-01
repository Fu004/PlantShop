package com.example.myweb.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billID;

    @ManyToOne
    @JoinColumn(name = "userID")
    private User user;

    private LocalDateTime createdAt;
    private String status;

    // Getters & Setters
}
