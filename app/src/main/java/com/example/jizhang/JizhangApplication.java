package com.example.jizhang;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategoryRepository;

public class JizhangApplication extends MultiDexApplication {
    private static final String TAG = "JizhangApplication";
    
    // 导出SQL文件的开关标志，默认为false
    public static boolean ENABLE_EXPORT_SQL = true;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化类别数据
        initCategories();
        
        // 在开发环境中可以手动设置为true
        // ENABLE_EXPORT_SQL = true;
    }
    
    private void initCategories() {
        // 检查是否已经初始化过类别数据
        SharedPreferences prefs = getSharedPreferences("jizhang_prefs", MODE_PRIVATE);
        boolean categoriesInitialized = prefs.getBoolean("categories_initialized", false);
        
        if (!categoriesInitialized) {
            Log.d(TAG, "初始化默认类别数据");
            CategoryRepository repository = new CategoryRepository(this);
            
            // 支出类别
            boolean added = repository.insert(new Category("餐饮", android.R.drawable.ic_menu_edit, Category.CategoryType.EXPENSE));
            Log.d(TAG, "添加餐饮类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("购物", android.R.drawable.ic_menu_gallery, Category.CategoryType.EXPENSE));
            Log.d(TAG, "添加购物类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("交通", android.R.drawable.ic_menu_directions, Category.CategoryType.EXPENSE));
            Log.d(TAG, "添加交通类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("娱乐", android.R.drawable.ic_menu_view, Category.CategoryType.EXPENSE));
            Log.d(TAG, "添加娱乐类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("医疗", android.R.drawable.ic_menu_help, Category.CategoryType.EXPENSE));
            Log.d(TAG, "添加医疗类别: " + (added ? "成功" : "已存在"));
            
            // 收入类别
            added = repository.insert(new Category("工资", android.R.drawable.ic_menu_agenda, Category.CategoryType.INCOME));
            Log.d(TAG, "添加工资类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("奖金", android.R.drawable.ic_menu_compass, Category.CategoryType.INCOME));
            Log.d(TAG, "添加奖金类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("理财", android.R.drawable.ic_menu_slideshow, Category.CategoryType.INCOME));
            Log.d(TAG, "添加理财类别: " + (added ? "成功" : "已存在"));
            
            added = repository.insert(new Category("其他", android.R.drawable.ic_menu_more, Category.CategoryType.INCOME));
            Log.d(TAG, "添加其他类别: " + (added ? "成功" : "已存在"));
            
            // 标记为已初始化
            prefs.edit().putBoolean("categories_initialized", true).apply();
            Log.d(TAG, "类别初始化完成");
        }
    }
} 