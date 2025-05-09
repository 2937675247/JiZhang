package com.example.jizhang.ui.expense;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class ExpenseDetailsViewModel extends AndroidViewModel {

    private final TransactionRepository mRepository;
    private final LiveData<List<Transaction>> mExpenseTransactions;
    private final LiveData<Double> mTotalExpense;

    public ExpenseDetailsViewModel(@NonNull Application application) {
        super(application);
        mRepository = new TransactionRepository(application);
        mExpenseTransactions = mRepository.getTransactionsByType(Transaction.TransactionType.EXPENSE);
        mTotalExpense = mRepository.getTotalByType(Transaction.TransactionType.EXPENSE);
    }

    /**
     * 获取所有支出交易
     */
    public LiveData<List<Transaction>> getExpenseTransactions() {
        return mExpenseTransactions;
    }

    /**
     * 获取总支出
     */
    public LiveData<Double> getTotalExpense() {
        return mTotalExpense;
    }
    
    /**
     * 删除交易记录
     * @param transaction 要删除的交易记录
     */
    public void deleteTransaction(Transaction transaction) {
        mRepository.delete(transaction);
    }
} 