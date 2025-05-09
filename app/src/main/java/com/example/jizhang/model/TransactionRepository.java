package com.example.jizhang.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TransactionRepository {
    private final TransactionDao mTransactionDao;
    private final LiveData<List<Transaction>> mAllTransactions;
    private final LiveData<Double> mTotalIncome;
    private final LiveData<Double> mTotalExpense;
    private final LiveData<Double> mTotalDebt;
    private final AppDatabase mDb;

    public TransactionRepository(Application application) {
        mDb = AppDatabase.getDatabase(application);
        mTransactionDao = mDb.transactionDao();
        mAllTransactions = mTransactionDao.getAllTransactions();
        mTotalIncome = mTransactionDao.getTotalByType(Transaction.TransactionType.INCOME);
        mTotalExpense = mTransactionDao.getTotalByType(Transaction.TransactionType.EXPENSE);
        mTotalDebt = mTransactionDao.getTotalByType(Transaction.TransactionType.DEBT);
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return mAllTransactions;
    }
    
    /**
     * 同步获取所有交易记录
     * 注意：此方法不应在主线程中调用
     * @return 所有交易记录列表
     */
    public List<Transaction> getAllTransactionsSync() {
        Future<List<Transaction>> future = AppDatabase.databaseWriteExecutor.submit(() -> 
            mTransactionDao.getAllTransactionsSync());
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type) {
        return mTransactionDao.getTransactionsByType(type);
    }

    public LiveData<List<Transaction>> getDebtTransactionsByType(Transaction.DebtType debtType) {
        return mTransactionDao.getDebtTransactionsByType(Transaction.TransactionType.DEBT, debtType);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return mTransactionDao.getTransactionsBetweenDates(startDate, endDate);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDatesByDay(Date startDate, Date endDate) {
        return mTransactionDao.getTransactionsBetweenDatesByDay(startDate, endDate);
    }

    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return mTransactionDao.getTransactionsByCategory(category);
    }

    public LiveData<Double> getTotalByType(Transaction.TransactionType type) {
        return mTransactionDao.getTotalByType(type);
    }

    public LiveData<Double> getTotalByDebtType(Transaction.DebtType debtType) {
        return mTransactionDao.getTotalByDebtType(Transaction.TransactionType.DEBT, debtType);
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
        return mTransactionDao.getActualBalance();
    }

    public LiveData<List<TransactionDao.CategoryTotal>> getTotalByCategory(Transaction.TransactionType type) {
        return mTransactionDao.getTotalByCategory(type);
    }

    public LiveData<List<Transaction>> getDebtTransactions() {
        return mTransactionDao.getDebtTransactions();
    }

    public void insert(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTransactionDao.insert(transaction);
        });
    }

    public void update(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTransactionDao.update(transaction);
        });
    }

    public void delete(Transaction transaction) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mTransactionDao.delete(transaction);
        });
    }

    /**
     * 刷新数据库数据
     * 使用LiveData自动观察数据变更，不需要手动刷新
     * Room 2.6.1已经不支持refreshAllTables()方法
     */
    public void refresh() {
        // 在Room 2.6.1中，不再需要手动刷新LiveData
        // LiveData已经通过DAO查询自动注册了数据库观察者，当数据变化时会自动更新
        
        // 如果有特定需要手动刷新的场景，可以考虑以下几种方式：
        // 1. 重新执行相关查询
        // 2. 使用MutableLiveData的setValue/postValue方法触发更新
        // 3. 在数据库写操作后，通过回调机制通知UI更新
        
        // 注意：此方法保留为空实现，仅作为向后兼容
    }
} 