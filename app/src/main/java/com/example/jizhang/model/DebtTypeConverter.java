package com.example.jizhang.model;

import androidx.room.TypeConverter;
import com.example.jizhang.model.Transaction.DebtType;

public class DebtTypeConverter {
    @TypeConverter
    public static DebtType toDebtType(String value) {
        return value == null ? null : DebtType.valueOf(value);
    }

    @TypeConverter
    public static String fromDebtType(DebtType type) {
        return type == null ? null : type.name();
    }
} 