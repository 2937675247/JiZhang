package com.example.jizhang.ui.expense;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.example.jizhang.model.TransactionRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddExpenseActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {
    private AddExpenseViewModel mViewModel;
    private TextInputEditText mAmountEditText;
    private TextInputEditText mDateEditText;
    private TextInputEditText mNoteEditText;
    private TextInputEditText mTransactionNoEditText;
    private CategoryAdapter mAdapter;
    private String mSelectedCategory;
    private Date mSelectedDate;
    private final Calendar mCalendar = Calendar.getInstance();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private CategoryRepository mCategoryRepository;
    private TransactionRepository mTransactionRepository;
    
    // 编辑模式相关变量
    private boolean mIsEditMode = false;
    private long mTransactionId = -1;
    private Transaction mCurrentTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // 初始化Repository
        mCategoryRepository = new CategoryRepository(getApplication());
        mTransactionRepository = new TransactionRepository(getApplication());

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 初始化视图
        mAmountEditText = findViewById(R.id.amountEditText);
        mDateEditText = findViewById(R.id.dateEditText);
        mNoteEditText = findViewById(R.id.noteEditText);
        mTransactionNoEditText = findViewById(R.id.transactionNoEditText);
        Button saveButton = findViewById(R.id.saveButton);
        Button repaymentButton = findViewById(R.id.repaymentButton);

        // 设置当前日期
        mSelectedDate = new Date();
        mDateEditText.setText(mDateFormat.format(mSelectedDate));
        mDateEditText.setOnClickListener(v -> showDatePicker());

        // 设置RecyclerView
        RecyclerView categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        mAdapter = new CategoryAdapter(this, this);
        categoriesRecyclerView.setAdapter(mAdapter);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // 初始化ViewModel并观察数据变化
        mViewModel = new ViewModelProvider(this).get(AddExpenseViewModel.class);
        mViewModel.getExpenseCategories().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                mAdapter.setCategories(categories);
                if (mSelectedCategory == null) {
                    mSelectedCategory = categories.get(0).getName();
                    mAdapter.setSelectedCategory(mSelectedCategory);
                } else {
                    // 如果是编辑模式，设置已选择的类别
                    mAdapter.setSelectedCategory(mSelectedCategory);
                }
            }
        });

        // 检查是否为编辑模式
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("is_edit_mode")) {
            mIsEditMode = intent.getBooleanExtra("is_edit_mode", false);
            if (mIsEditMode && intent.hasExtra("transaction_id")) {
                mTransactionId = intent.getLongExtra("transaction_id", -1);
                if (mTransactionId != -1) {
                    // 加载交易数据
                    loadTransactionData(mTransactionId);
                    
                    // 更新标题
                    getSupportActionBar().setTitle("编辑支出");
                    
                    // 更新保存按钮文本
                    saveButton.setText("更新");
                    
                    // 在编辑模式下隐藏还款按钮
                    repaymentButton.setVisibility(View.GONE);
                }
            }
        }

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            if (mIsEditMode) {
                updateExpense();
            } else {
                saveExpense();
            }
        });
        
        // 设置还款按钮点击事件
        repaymentButton.setOnClickListener(v -> showDebtRepaymentDialog());
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

    /**
     * 加载交易数据
     */
    private void loadTransactionData(long transactionId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // 从数据库中获取交易记录
            List<Transaction> transactions = mTransactionRepository.getAllTransactionsSync();
            for (Transaction transaction : transactions) {
                if (transaction.getId() == transactionId) {
                    mCurrentTransaction = transaction;
                    break;
                }
            }
            
            // 在UI线程中更新界面
            runOnUiThread(() -> {
                if (mCurrentTransaction != null) {
                    // 填充表单数据
                    mAmountEditText.setText(String.valueOf(mCurrentTransaction.getAmount()));
                    mNoteEditText.setText(mCurrentTransaction.getDescription());
                    mSelectedDate = mCurrentTransaction.getDate();
                    mDateEditText.setText(mDateFormat.format(mSelectedDate));
                    mSelectedCategory = mCurrentTransaction.getCategory();
                    mTransactionNoEditText.setText(mCurrentTransaction.getPaymentTransactionNo());
                }
            });
        });
        executor.shutdown();
    }

    private void saveExpense() {
        if (validateInput()) {
            double amount = Double.parseDouble(mAmountEditText.getText().toString());
            String note = mNoteEditText.getText().toString();
            String transactionNo = mTransactionNoEditText.getText() != null ? 
                    mTransactionNoEditText.getText().toString() : "";
            
            Transaction expense = new Transaction(
                    0,
                    amount,
                    note,
                    mSelectedDate,
                    mSelectedCategory,
                    Transaction.TransactionType.EXPENSE,
                    transactionNo
            );
            
            mViewModel.insert(expense);
            Toast.makeText(this, "支出已保存", Toast.LENGTH_SHORT).show();
            
            // 发送广播通知数据已更改
            Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
            sendBroadcast(intent);
            
            finish();
        }
    }
    
    /**
     * 更新支出记录
     */
    private void updateExpense() {
        if (validateInput() && mCurrentTransaction != null) {
            double amount = Double.parseDouble(mAmountEditText.getText().toString());
            String note = mNoteEditText.getText().toString();
            String transactionNo = mTransactionNoEditText.getText() != null ? 
                    mTransactionNoEditText.getText().toString() : "";
            
            // 更新当前交易记录的数据
            mCurrentTransaction.setAmount(amount);
            mCurrentTransaction.setDescription(note);
            mCurrentTransaction.setDate(mSelectedDate);
            mCurrentTransaction.setCategory(mSelectedCategory);
            mCurrentTransaction.setPaymentTransactionNo(transactionNo);
            
            // 更新数据库
            mTransactionRepository.update(mCurrentTransaction);
            
            Toast.makeText(this, "支出已更新", Toast.LENGTH_SHORT).show();
            
            // 发送广播通知数据已更改
            Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
            sendBroadcast(intent);
            
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
    
    // 显示负债还款对话框
    private void showDebtRepaymentDialog() {
        // 保存当前输入的数据，以便于返回填写
        final String currentAmount = mAmountEditText.getText() != null ? mAmountEditText.getText().toString() : "";
        final String currentNote = mNoteEditText.getText() != null ? mNoteEditText.getText().toString() : "";
        final String currentTransactionNo = mTransactionNoEditText.getText() != null ? mTransactionNoEditText.getText().toString() : "";
        
        // 跳转到负债还款界面
        Intent intent = new Intent(this, com.example.jizhang.ui.debt.AddDebtRepaymentActivity.class);
        startActivity(intent);
        
        // 因为是还款操作，用户可能不再返回添加支出界面，所以关闭当前界面
        finish();
    }
} 