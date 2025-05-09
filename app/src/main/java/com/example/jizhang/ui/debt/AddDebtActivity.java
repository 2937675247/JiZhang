package com.example.jizhang.ui.debt;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategoryRepository;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.ui.expense.CategoryAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddDebtActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private AddDebtViewModel mViewModel;
    private TextInputEditText mAmountEditText;
    private TextInputEditText mDateEditText;
    private TextInputEditText mNoteEditText;
    private TextInputEditText mTransactionNoEditText;
    private CategoryAdapter mAdapter;
    private Spinner mDebtTypeSpinner;
    private String mSelectedCategory;
    private Date mSelectedDate;
    private Transaction.DebtType mSelectedDebtType = Transaction.DebtType.HUABEI; // 默认选择花呗
    private final Calendar mCalendar = Calendar.getInstance();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private CategoryRepository mCategoryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        // 初始化CategoryRepository
        mCategoryRepository = new CategoryRepository(getApplication());

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化视图
        mAmountEditText = findViewById(R.id.amountEditText);
        mDateEditText = findViewById(R.id.dateEditText);
        mNoteEditText = findViewById(R.id.noteEditText);
        mTransactionNoEditText = findViewById(R.id.transactionNoEditText);
        mDebtTypeSpinner = findViewById(R.id.debtTypeSpinner);
        Button saveButton = findViewById(R.id.saveButton);

        // 设置当前日期
        mSelectedDate = new Date();
        mDateEditText.setText(mDateFormat.format(mSelectedDate));
        mDateEditText.setOnClickListener(v -> showDatePicker());

        // 设置负债类型下拉菜单
        setupDebtTypeSpinner();

        // 设置RecyclerView
        RecyclerView categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        mAdapter = new CategoryAdapter(this, this);
        categoriesRecyclerView.setAdapter(mAdapter);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 初始化ViewModel并观察数据变化
        mViewModel = new ViewModelProvider(this).get(AddDebtViewModel.class);
        mViewModel.getExpenseCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                mAdapter.setCategories(categories);
                if (mSelectedCategory == null) {
                    mSelectedCategory = categories.get(0).getName();
                    mAdapter.setSelectedCategory(mSelectedCategory);
                }
            }
        });

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> saveDebt());
    }

    private void setupDebtTypeSpinner() {
        // 创建负债类型适配器
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        adapter.add("花呗");
        adapter.add("白条");
        adapter.add("信用卡");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // 设置Spinner
        mDebtTypeSpinner.setAdapter(adapter);
        mDebtTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mSelectedDebtType = Transaction.DebtType.HUABEI;
                        break;
                    case 1:
                        mSelectedDebtType = Transaction.DebtType.BAITIAO;
                        break;
                    case 2:
                        mSelectedDebtType = Transaction.DebtType.CREDIT_CARD;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认选择花呗
                mSelectedDebtType = Transaction.DebtType.HUABEI;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCategoryClick(Category category) {
        mSelectedCategory = category.getName();
        mAdapter.setSelectedCategory(mSelectedCategory);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    mCalendar.set(Calendar.YEAR, year);
                    mCalendar.set(Calendar.MONTH, month);
                    mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    mSelectedDate = mCalendar.getTime();
                    mDateEditText.setText(mDateFormat.format(mSelectedDate));
                },
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveDebt() {
        if (validateInput()) {
            double amount = Double.parseDouble(mAmountEditText.getText().toString());
            String note = mNoteEditText.getText().toString();
            String transactionNo = mTransactionNoEditText.getText() != null ? 
                    mTransactionNoEditText.getText().toString() : "";
            
            Transaction debt = new Transaction(
                    0,
                    amount,
                    note,
                    mSelectedDate,
                    mSelectedCategory,
                    Transaction.TransactionType.DEBT,
                    transactionNo,
                    mSelectedDebtType
            );
            
            mViewModel.insert(debt);
            Toast.makeText(this, "负债已记录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        
        if (TextUtils.isEmpty(mAmountEditText.getText())) {
            mAmountEditText.setError("请输入金额");
            valid = false;
        }
        
        if (mSelectedCategory == null) {
            Toast.makeText(this, "请选择类别", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        
        return valid;
    }
} 