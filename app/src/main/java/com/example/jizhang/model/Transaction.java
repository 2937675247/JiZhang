package com.example.jizhang.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Date;

@Entity(tableName = "transactions")
@TypeConverters({DateConverter.class, TransactionTypeConverter.class, DebtTypeConverter.class})
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private double amount;
    private String description;
    private Date date;
    private String category;
    private TransactionType type;
    private String paymentTransactionNo; // 支付平台交易单号（支付宝/微信/银行）
    private DebtType debtType; // 负债类型，如花呗、白条、信用卡，仅当type为DEBT时有意义

    @Ignore
    public Transaction(long id, double amount, String description, Date date, String category, TransactionType type) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
        this.type = type;
        this.paymentTransactionNo = ""; // 默认为空字符串
        this.debtType = DebtType.NONE; // 默认为无负债类型
    }
    
    // 带交易单号的构造函数
    public Transaction(long id, double amount, String description, Date date, String category, TransactionType type, String paymentTransactionNo) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
        this.type = type;
        this.paymentTransactionNo = paymentTransactionNo;
        this.debtType = DebtType.NONE; // 默认为无负债类型
    }
    
    // 带负债类型的构造函数
    @Ignore
    public Transaction(long id, double amount, String description, Date date, String category, TransactionType type, String paymentTransactionNo, DebtType debtType) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
        this.type = type;
        this.paymentTransactionNo = paymentTransactionNo;
        this.debtType = debtType;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
    
    public String getPaymentTransactionNo() {
        return paymentTransactionNo;
    }

    public void setPaymentTransactionNo(String paymentTransactionNo) {
        this.paymentTransactionNo = paymentTransactionNo;
    }
    
    public DebtType getDebtType() {
        return debtType;
    }
    
    public void setDebtType(DebtType debtType) {
        this.debtType = debtType;
    }
    
    public boolean isExpense() {
        return type == TransactionType.EXPENSE;
    }
    
    public boolean isDebt() {
        return type == TransactionType.DEBT;
    }

    public enum TransactionType {
        INCOME,
        EXPENSE,
        DEBT    // 新增负债类型
    }
    
    public enum DebtType {
        NONE,           // 无负债类型（非负债交易）
        HUABEI,         // 花呗
        BAITIAO,        // 白条
        CREDIT_CARD     // 信用卡
    }
} 