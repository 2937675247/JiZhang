package com.example.jizhang.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("DELETE FROM transactions")
    void deleteAllTransactions();

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions();

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByType(Transaction.TransactionType type);

    @Query("SELECT * FROM transactions WHERE type = :type AND debtType = :debtType ORDER BY date DESC")
    LiveData<List<Transaction>> getDebtTransactionsByType(Transaction.TransactionType type, Transaction.DebtType debtType);

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate);

    @Query("SELECT * FROM transactions WHERE strftime('%Y-%m-%d', date / 1000, 'unixepoch') >= strftime('%Y-%m-%d', :startDate / 1000, 'unixepoch') AND strftime('%Y-%m-%d', date / 1000, 'unixepoch') <= strftime('%Y-%m-%d', :endDate / 1000, 'unixepoch') ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsBetweenDatesByDay(Date startDate, Date endDate);

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    LiveData<List<Transaction>> getTransactionsByCategory(String category);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type")
    LiveData<Double> getTotalByType(Transaction.TransactionType type);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND debtType = :debtType")
    LiveData<Double> getTotalByDebtType(Transaction.TransactionType type, Transaction.DebtType debtType);

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalByTypeAndDateRange(Transaction.TransactionType type, Date startDate, Date endDate);

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getTotalByCategory(Transaction.TransactionType type);

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = :type AND debtType = :debtType GROUP BY category ORDER BY total DESC")
    LiveData<List<CategoryTotal>> getTotalByCategoryAndDebtType(Transaction.TransactionType type, Transaction.DebtType debtType);

    @Query("SELECT (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME') - (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE') AS balance")
    LiveData<Double> getActualBalance();

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactionsSync();

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    List<Transaction> getTransactionsByTypeSync(Transaction.TransactionType type);

    @Query("SELECT * FROM transactions WHERE type = 'DEBT' ORDER BY date DESC")
    LiveData<List<Transaction>> getDebtTransactions();

    static class CategoryTotal {
        public String category;
        public double total;
    }
} 