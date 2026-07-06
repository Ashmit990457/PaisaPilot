package com.example.paisapilot.model;

import java.util.List;

public class ReceiptResult {
    private String merchant;
    private String title;
    private double amount;
    private String currency;
    private String category;
    private String paymentMethod;
    private String date; // YYYY-MM-DD
    private String time;
    private double tax;
    private int confidence;
    private List<ReceiptItem> items;

    public static class ReceiptItem {
        private String name;
        private double price;

        public ReceiptItem() {}
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public ReceiptResult() {}

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public int getConfidence() { return confidence; }
    public void setConfidence(int confidence) { this.confidence = confidence; }
    public List<ReceiptItem> getItems() { return items; }
    public void setItems(List<ReceiptItem> items) { this.items = items; }
}
