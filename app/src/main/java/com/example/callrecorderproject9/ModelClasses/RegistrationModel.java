package com.example.callrecorderproject9.ModelClasses;

public class RegistrationModel {
    private String name, email, phoneNumber,userID;

    public RegistrationModel() {
    }

    public RegistrationModel(String name, String email, String phoneNumber, String userID) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
