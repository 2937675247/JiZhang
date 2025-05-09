package com.example.jizhang.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories")
    void deleteAllCategories();

    @Query("DELETE FROM categories WHERE name NOT IN ('餐饮', '交通', '购物', '娱乐', '住房', '通讯', '医疗', '教育', '其他') AND type = 'EXPENSE'")
    void deleteCustomExpenseCategories();

    @Query("DELETE FROM categories WHERE name NOT IN ('工资', '奖金', '投资', '兼职', '其他') AND type = 'INCOME'")
    void deleteCustomIncomeCategories();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(Category.CategoryType type);

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    LiveData<Category> getCategoryById(long id);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    LiveData<Category> getCategoryByName(String name);

    @Query("SELECT * FROM categories WHERE name = :name AND type = :type LIMIT 1")
    Category getCategoryByNameAndTypeSync(String name, Category.CategoryType type);
} 