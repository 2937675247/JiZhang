package com.example.jizhang.ui.income;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategoryRepository;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class AddIncomeViewModel extends AndroidViewModel {

    private final TransactionRepository mTransactionRepository;
    private final CategoryRepository mCategoryRepository;
    private final LiveData<List<Category>> mIncomeCategories;

    public AddIncomeViewModel(@NonNull Application application) {
        super(application);
        mTransactionRepository = new TransactionRepository(application);
        mCategoryRepository = new CategoryRepository(application);
        mIncomeCategories = mCategoryRepository.getCategoriesByType(Category.CategoryType.INCOME);
    }

    public LiveData<List<Category>> getIncomeCategories() {
        return mIncomeCategories;
    }

    public void insert(Transaction transaction) {
        mTransactionRepository.insert(transaction);
    }
} 