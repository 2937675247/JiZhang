package com.example.jizhang.ui.income;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class IncomeDetailsViewModel extends AndroidViewModel {

    private final TransactionRepository mRepository;
    private final LiveData<List<Transaction>> mIncomeTransactions;
    private final LiveData<Double> mTotalIncome;

    public IncomeDetailsViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TransactionRepository(application);
        mIncomeTransactions = mRepository.getTransactionsByType(Transaction.TransactionType.INCOME);
        mTotalIncome = mRepository.getTotalByType(Transaction.TransactionType.INCOME);
    }

    /**
     * 获取所有收入交易
     */
    public LiveData<List<Transaction>> getIncomeTransactions() {
        return mIncomeTransactions;
    }

    /**
     * 获取总收入
     */
    public LiveData<Double> getTotalIncome() {
        return mTotalIncome;
    }
    
    /**
     * 删除交易记录
     * @param transaction 要删除的交易记录
     */
    public void deleteTransaction(Transaction transaction) {
        mRepository.delete(transaction);
    }
} 