package com.example.jizhang.model;

import androidx.room.TypeConverter;
import com.example.jizhang.model.Transaction.TransactionType;

public class TransactionTypeConverter {
    @TypeConverter
    public static TransactionType toTransactionType(String value) {
        return value == null ? null : TransactionType.valueOf(value);
    }

    @TypeConverter
    public static String fromTransactionType(TransactionType type) {
        return type == null ? null : type.name();
    }
}