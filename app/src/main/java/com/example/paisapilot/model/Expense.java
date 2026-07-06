package com.example.paisapilot.model;

import com.google.firebase.Timestamp;

/**
 * Expense model for Firestore documents.
 * Includes empty constructor and getters/setters for Firestore compatibility.
 */
public class Expense {
    private String expenseId;
    private String title;
    private String category;
    private double amount;
    private Timestamp date;
    private String note;
    private String paymentMethod;
    private String userId;
    private Timestamp createdAt;

    public Expense() {
        // Required empty constructor for Firestore deserialization
    }

    public Expense(String expenseId, String title, String category, double amount, Timestamp date, String note, String paymentMethod, String userId, Timestamp createdAt) {
        this.expenseId = expenseId;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
