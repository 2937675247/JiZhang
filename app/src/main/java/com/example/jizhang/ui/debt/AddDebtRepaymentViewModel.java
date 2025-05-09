package com.example.jizhang.ui.debt;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

public class AddDebtRepaymentViewModel extends AndroidViewModel {
    private final TransactionRepository mTransactionRepository;

    public AddDebtRepaymentViewModel(@NonNull Application application) {
        super(application);
        mTransactionRepository = new TransactionRepository(application);
    }

    public void insert(Transaction transaction) {
        mTransactionRepository.insert(transaction);
    }
} 