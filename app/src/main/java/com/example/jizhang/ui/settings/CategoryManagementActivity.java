package com.example.jizhang.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategoryRepository;
import com.example.jizhang.ui.helper.SwipeToDeleteCallback;

import java.util.List;

public class CategoryManagementActivity extends AppCompatActivity implements View.OnClickListener {
    
    private CategoryRepository mCategoryRepository;
    
    private RecyclerView mRecyclerViewExpenseCategories;
    private CategoryAdapter mExpenseCategoryAdapter;
    
    private RecyclerView mRecyclerViewIncomeCategories;
    private CategoryAdapter mIncomeCategoryAdapter;
    
    private Toolbar mToolbar;
    private EditText mEditTextExpenseCategory;
    private EditText mEditTextIncomeCategory;
    private Button mBtnAddExpenseCategory;
    private Button mBtnAddIncomeCategory;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_management);
        
        // 初始化Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("类别管理");
        }
        
        // 初始化UI组件
        mEditTextExpenseCategory = findViewById(R.id.categoryNameEditText);
        mEditTextIncomeCategory = findViewById(R.id.incomeCategoryNameEditText);
        mBtnAddExpenseCategory = findViewById(R.id.addCategoryButton);
        mBtnAddIncomeCategory = findViewById(R.id.addIncomeCategoryButton);
        mRecyclerViewExpenseCategories = findViewById(R.id.expenseCategoriesRecyclerView);
        mRecyclerViewIncomeCategories = findViewById(R.id.incomeCategoriesRecyclerView);
        
        // 设置RecyclerView
        mRecyclerViewExpenseCategories.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewIncomeCategories.setLayoutManager(new LinearLayoutManager(this));
        
        mExpenseCategoryAdapter = new CategoryAdapter(Category.CategoryType.EXPENSE);
        mIncomeCategoryAdapter = new CategoryAdapter(Category.CategoryType.INCOME);
        
        mRecyclerViewExpenseCategories.setAdapter(mExpenseCategoryAdapter);
        mRecyclerViewIncomeCategories.setAdapter(mIncomeCategoryAdapter);
        
        // 获取CategoryRepository实例
        mCategoryRepository = new CategoryRepository(getApplication());
        
        // 加载分类数据
        loadExpenseCategories();
        loadIncomeCategories();
        
        // 设置按钮点击监听器
        mBtnAddExpenseCategory.setOnClickListener(this);
        mBtnAddIncomeCategory.setOnClickListener(this);
    }
    
    /**
     * 加载支出类别数据
     */
    private void loadExpenseCategories() {
        LiveData<List<Category>> expenseCategoriesLiveData = mCategoryRepository.getCategoriesByType(Category.CategoryType.EXPENSE);
        expenseCategoriesLiveData.observe(this, categories -> {
            if (categories != null) {
                mExpenseCategoryAdapter.setCategories(categories);
            }
        });
        
        // 设置CategoryRepository
        mExpenseCategoryAdapter.setCategoryRepository(mCategoryRepository);
        
        // 添加滑动删除功能
        setupSwipeToDelete(mRecyclerViewExpenseCategories, mExpenseCategoryAdapter);
    }
    
    /**
     * 加载收入类别数据
     */
    private void loadIncomeCategories() {
        LiveData<List<Category>> incomeCategoriesLiveData = mCategoryRepository.getCategoriesByType(Category.CategoryType.INCOME);
        incomeCategoriesLiveData.observe(this, categories -> {
            if (categories != null) {
                mIncomeCategoryAdapter.setCategories(categories);
            }
        });
        
        // 设置CategoryRepository
        mIncomeCategoryAdapter.setCategoryRepository(mCategoryRepository);
        
        // 添加滑动删除功能
        setupSwipeToDelete(mRecyclerViewIncomeCategories, mIncomeCategoryAdapter);
    }
    
    /**
     * 设置滑动删除功能
     * @param recyclerView 目标RecyclerView
     * @param adapter 适配器
     */
    private void setupSwipeToDelete(RecyclerView recyclerView, CategoryAdapter adapter) {
        // 创建滑动删除回调
        SwipeToDeleteCallback swipeToDeleteCallback = 
            new SwipeToDeleteCallback(
                this,
                position -> adapter.deleteCategory(position)
            );
        
        // 创建ItemTouchHelper并附加到RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
    
    /**
     * 添加类别
     */
    private void addCategory(Category.CategoryType type) {
        String categoryName = type == Category.CategoryType.EXPENSE ? mEditTextExpenseCategory.getText().toString().trim() : mEditTextIncomeCategory.getText().toString().trim();
        
        if (TextUtils.isEmpty(categoryName)) {
            Toast.makeText(this, R.string.category_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建新的类别对象并添加到数据库
        Category newCategory = new Category(categoryName, android.R.drawable.ic_menu_edit, type);
        boolean added = mCategoryRepository.insert(newCategory);
        
        if (added) {
            // 添加成功，清空输入框
            if (type == Category.CategoryType.EXPENSE) {
                mEditTextExpenseCategory.setText("");
            } else {
                mEditTextIncomeCategory.setText("");
            }
            Toast.makeText(this, R.string.category_added, Toast.LENGTH_SHORT).show();
        } else {
            // 添加失败，可能是类别已存在
            Toast.makeText(this, R.string.category_exists, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.addCategoryButton) {
            addCategory(Category.CategoryType.EXPENSE);
        } else if (id == R.id.addIncomeCategoryButton) {
            addCategory(Category.CategoryType.INCOME);
        }
    }
    
    /**
     * 类别适配器
     */
    private static class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        
        private List<Category> mCategories;
        private final Category.CategoryType mType;
        private CategoryRepository mRepository;
        
        public CategoryAdapter(Category.CategoryType type) {
            mType = type;
        }
        
        public void setCategories(List<Category> categories) {
            mCategories = categories;
            notifyDataSetChanged();
        }
        
        public void setCategoryRepository(CategoryRepository repository) {
            mRepository = repository;
        }
        
        public void deleteCategory(int position) {
            if (position >= 0 && position < mCategories.size()) {
                Category category = mCategories.get(position);
                
                // 检查是否是默认类别，默认类别不允许删除
                if (isDefaultCategory(category)) {
                    // 通知用户默认类别不能删除
                    Toast.makeText(
                        CategoryViewHolder.lastItemView.getContext(), 
                        "默认类别不能删除", 
                        Toast.LENGTH_SHORT
                    ).show();
                    notifyItemChanged(position); // 恢复视图
                    return;
                }
                
                // 删除类别
                if (mRepository != null) {
                    mRepository.delete(category);
                    // 不需要手动从列表中移除，因为LiveData会自动更新
                }
            }
        }
        
        /**
         * 检查是否是默认类别
         */
        private boolean isDefaultCategory(Category category) {
            if (category.getType() == Category.CategoryType.EXPENSE) {
                String[] defaultExpenseCategories = {"餐饮", "交通", "购物", "娱乐", "住房", "通讯", "医疗", "教育", "其他"};
                for (String defaultCategory : defaultExpenseCategories) {
                    if (defaultCategory.equals(category.getName())) {
                        return true;
                    }
                }
            } else if (category.getType() == Category.CategoryType.INCOME) {
                String[] defaultIncomeCategories = {"工资", "奖金", "投资", "兼职", "其他"};
                for (String defaultCategory : defaultIncomeCategories) {
                    if (defaultCategory.equals(category.getName())) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new CategoryViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = mCategories.get(position);
            holder.bind(category);
        }
        
        @Override
        public int getItemCount() {
            return mCategories == null ? 0 : mCategories.size();
        }
        
        static class CategoryViewHolder extends RecyclerView.ViewHolder {
            private final android.widget.TextView textView;
            public static View lastItemView; // 用于Toast显示
            
            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = (android.widget.TextView) itemView.findViewById(android.R.id.text1);
                lastItemView = itemView; // 保存最后一个视图引用
            }
            
            public void bind(Category category) {
                textView.setText(category.getName());
            }
        }
    }
} 