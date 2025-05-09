package com.example.jizhang.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "categories",
    indices = {@Index(value = {"name", "type"}, unique = true)}
)
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private int iconResId;
    private CategoryType type;

    public Category(String name, int iconResId, CategoryType type) {
        this.name = name;
        this.iconResId = iconResId;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public enum CategoryType {
        INCOME,
        EXPENSE
    }
} 