package com.example.jizhang.ui.expense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategoryRepository;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class AddExpenseViewModel extends AndroidViewModel {
    private final TransactionRepository mTransactionRepository;
    private final CategoryRepository mCategoryRepository;
    private final LiveData<List<Category>> mExpenseCategories;

    public AddExpenseViewModel(@NonNull Application application) {
        super(application);
        mTransactionRepository = new TransactionRepository(application);
        mCategoryRepository = new CategoryRepository(application);
        mExpenseCategories = mCategoryRepository.getCategoriesByType(Category.CategoryType.EXPENSE);
    }

    public LiveData<List<Category>> getExpenseCategories() {
        return mExpenseCategories;
    }

    public void insert(Transaction transaction) {
        mTransactionRepository.insert(transaction);
    }
} 