package com.example.myweb.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


@Entity
public class User {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userID;

    private String userName;
    private String userPassword;
    private Boolean isSeller;

    // Getters/setters
    public String getUserName(){
        return userName;
    }
    public Long getUserID(){
        return userID;
    }
    public void setUserID(){
        this.userID = userID;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setUserID(Long userID){
        this.userID = userID;
    }
    public void setUserPassword(String userPassword){
        this.userPassword = userPassword;
    }
    public void setIsSeller(Boolean isSeller){
        this.isSeller = isSeller;
    }
}