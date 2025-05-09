package com.example.jizhang;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final TransactionRepository mRepository;
    private final LiveData<List<Transaction>> mAllTransactions;
    private final LiveData<Double> mTotalIncome;
    private final LiveData<Double> mTotalExpense;
    private final LiveData<Double> mTotalDebt;
    private final LiveData<Double> mActualBalance;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new TransactionRepository(application);
        mAllTransactions = mRepository.getAllTransactions();
        mTotalIncome = mRepository.getTotalIncome();
        mTotalExpense = mRepository.getTotalExpense();
        mTotalDebt = mRepository.getTotalDebt();
        mActualBalance = mRepository.getActualBalance();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return mAllTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return mTotalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return mTotalExpense;
    }
    
    public LiveData<Double> getTotalDebt() {
        return mTotalDebt;
    }

    public LiveData<Double> getActualBalance() {
        return mActualBalance;
    }
} 