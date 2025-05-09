package com.example.jizhang.model;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CategoryRepository {
    private final CategoryDao mCategoryDao;
    private static final String TAG = "CategoryRepository";

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCategoryDao = db.categoryDao();
    }

    public LiveData<List<Category>> getCategoriesByType(Category.CategoryType type) {
        return mCategoryDao.getCategoriesByType(type);
    }

    public LiveData<Category> getCategoryById(long id) {
        return mCategoryDao.getCategoryById(id);
    }

    public LiveData<Category> getCategoryByName(String name) {
        return mCategoryDao.getCategoryByName(name);
    }

    /**
     * 插入类别，先检查是否已存在相同名称和类型的类别
     * @param category 要插入的类别
     * @return 是否插入成功
     */
    public boolean insert(Category category) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() -> {
                Category existingCategory = mCategoryDao.getCategoryByNameAndTypeSync(category.getName(), category.getType());
                if (existingCategory != null) {
                    // 已存在相同名称和类型的类别，不再插入
                    Log.d(TAG, "类别已存在: " + category.getName() + " - " + category.getType());
                    return false;
                }
                
                // 不存在相同类别，执行插入
                mCategoryDao.insert(category);
                return true;
            });
            return future.get(); // 返回插入结果
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "插入类别时出错", e);
            return false;
        }
    }

    public void update(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.update(category);
        });
    }

    public void delete(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.delete(category);
        });
    }
} 