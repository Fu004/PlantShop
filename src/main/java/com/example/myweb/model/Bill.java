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
    private User user;  // Đúng tên biến đại diện cho object User

    private LocalDateTime createdAt;
    private String status;
    private String information;

    // Getters and Setters

    public Long getBillID() {
        return billID;
    }

    public void setBillID(Long billID) {
        this.billID = billID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public Long getUserID() {
        return user != null ? user.getUserID() : null;
    }

    public void setUserID(int userID) {
        this.user = new User();
        this.user.setUserID((long) userID); // nếu UserID là Long
    }
}
