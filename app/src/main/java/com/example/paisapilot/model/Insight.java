package com.example.paisapilot.model;

public class Insight {
    public enum Type {
        WARNING, SUCCESS, INFO, TIP
    }

    private String title;
    private String description;
    private Type type;
    private int priority; // Higher value means higher priority

    public Insight(String title, String description, Type type, int priority) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Type getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }
}
