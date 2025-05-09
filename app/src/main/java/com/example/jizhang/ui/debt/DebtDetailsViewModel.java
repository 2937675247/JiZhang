package com.example.jizhang.ui.debt;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class DebtDetailsViewModel extends AndroidViewModel {
    private final TransactionRepository mRepository;
    private final LiveData<List<Transaction>> mDebtTransactions;
    private final LiveData<Double> mTotalDebt;

    public DebtDetailsViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TransactionRepository(application);
        
        // 获取债务交易数据
        mDebtTransactions = mRepository.getDebtTransactions();
        
        // 获取总债务金额
        mTotalDebt = mRepository.getTotalByType(Transaction.TransactionType.DEBT);
    }

    public LiveData<List<Transaction>> getDebtTransactions() {
        return mDebtTransactions;
    }

    public LiveData<Double> getTotalDebt() {
        return mTotalDebt;
    }

    public void deleteTransaction(Transaction transaction) {
        mRepository.delete(transaction);
    }
    
    // 添加插入交易的方法
    public void insert(Transaction transaction) {
        mRepository.insert(transaction);
    }
} 