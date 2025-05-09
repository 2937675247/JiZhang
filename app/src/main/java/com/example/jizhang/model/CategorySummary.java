package com.example.jizhang.model;

/**
 * 类别汇总数据模型，用于统计界面显示每个类别的消费/收入情况
 */
public class CategorySummary {
    public enum Type {INCOME, EXPENSE}
    
    private String categoryName;
    private double amount;
    private float percentage; // 该类别占总金额的百分比(0.0-1.0)
    private Type type;

    public CategorySummary(String categoryName, double amount, float percentage) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.percentage = percentage;
    }

    public CategorySummary(String categoryName, double amount, Type type) {
        this.categoryName = categoryName;
        this.amount = amount;
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
} 