package com.example.jizhang.ui.debt;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.jizhang.R;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddDebtRepaymentActivity extends AppCompatActivity {
    private AddDebtRepaymentViewModel mViewModel;
    private TextInputEditText mAmountEditText;
    private TextInputEditText mDateEditText;
    private TextInputEditText mNoteEditText;
    private TextInputEditText mTransactionNoEditText;
    private RadioGroup mDebtTypeRadioGroup;
    private RadioButton mHuabeiRadioButton;
    private RadioButton mBaitiaoRadioButton;
    private RadioButton mCreditCardRadioButton;
    private Date mSelectedDate;
    private Transaction.DebtType mSelectedDebtType = Transaction.DebtType.HUABEI; // 默认选择花呗
    private final Calendar mCalendar = Calendar.getInstance();
    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private TransactionRepository mTransactionRepository;
    
    // 编辑模式相关变量
    private boolean mIsEditMode = false;
    private long mTransactionId = -1;
    private Transaction mCurrentTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt_repayment);

        // 初始化ViewModel和Repository
        mViewModel = new ViewModelProvider(this).get(AddDebtRepaymentViewModel.class);
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
        
        // 初始化负债类型选择
        mDebtTypeRadioGroup = findViewById(R.id.debtTypeRadioGroup);
        mHuabeiRadioButton = findViewById(R.id.huabeiRadioButton);
        mBaitiaoRadioButton = findViewById(R.id.baitiaoRadioButton);
        mCreditCardRadioButton = findViewById(R.id.creditCardRadioButton);

        // 设置当前日期
        mSelectedDate = new Date();
        mDateEditText.setText(mDateFormat.format(mSelectedDate));
        mDateEditText.setOnClickListener(v -> showDatePicker());

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
                    getSupportActionBar().setTitle("编辑负债");
                    
                    // 更新保存按钮文本
                    saveButton.setText("更新");
                }
            }
        }

        // 设置保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            if (mIsEditMode) {
                updateDebt();
            } else {
                saveRepayment();
            }
        });
        
        // 设置负债类型选择事件
        mDebtTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.huabeiRadioButton) {
                mSelectedDebtType = Transaction.DebtType.HUABEI;
            } else if (checkedId == R.id.baitiaoRadioButton) {
                mSelectedDebtType = Transaction.DebtType.BAITIAO;
            } else if (checkedId == R.id.creditCardRadioButton) {
                mSelectedDebtType = Transaction.DebtType.CREDIT_CARD;
            }
        });
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
                    mAmountEditText.setText(String.valueOf(Math.abs(mCurrentTransaction.getAmount()))); // 使用绝对值
                    mNoteEditText.setText(mCurrentTransaction.getDescription());
                    mSelectedDate = mCurrentTransaction.getDate();
                    mDateEditText.setText(mDateFormat.format(mSelectedDate));
                    mTransactionNoEditText.setText(mCurrentTransaction.getPaymentTransactionNo());
                    
                    // 设置负债类型
                    mSelectedDebtType = mCurrentTransaction.getDebtType();
                    switch (mSelectedDebtType) {
                        case HUABEI:
                            mHuabeiRadioButton.setChecked(true);
                            break;
                        case BAITIAO:
                            mBaitiaoRadioButton.setChecked(true);
                            break;
                        case CREDIT_CARD:
                            mCreditCardRadioButton.setChecked(true);
                            break;
                    }
                }
            });
        });
        executor.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void saveRepayment() {
        if (validateInput()) {
            double amount = Double.parseDouble(mAmountEditText.getText().toString());
            String note = mNoteEditText.getText().toString();
            String transactionNo = mTransactionNoEditText.getText() != null ? 
                    mTransactionNoEditText.getText().toString() : "";
            
            // 创建支出交易记录
            Transaction expense = new Transaction(
                    0,
                    amount,
                    note + " (还款: " + getDebtTypeLabel(mSelectedDebtType) + ")",
                    mSelectedDate,
                    "还款",  // 使用"还款"作为类别
                    Transaction.TransactionType.EXPENSE,
                    transactionNo
            );
            
            // 保存支出交易
            mViewModel.insert(expense);
            
            // 创建负债减少记录（金额为负数）
            Transaction debtReduction = new Transaction(
                    0,
                    -amount, // 负数金额，表示减少负债
                    "还款减少负债: " + note,
                    mSelectedDate,
                    "还款",
                    Transaction.TransactionType.DEBT, // 类型仍然是负债
                    transactionNo,
                    mSelectedDebtType // 使用选择的负债类型
            );
            
            // 保存负债减少记录
            mViewModel.insert(debtReduction);
            
            // 发送广播通知数据已更改
            Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
            sendBroadcast(intent);
            
            Toast.makeText(this, R.string.repayment_success, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    /**
     * 更新负债记录
     */
    private void updateDebt() {
        if (validateInput() && mCurrentTransaction != null) {
            double amount = Double.parseDouble(mAmountEditText.getText().toString());
            String note = mNoteEditText.getText().toString();
            String transactionNo = mTransactionNoEditText.getText() != null ? 
                    mTransactionNoEditText.getText().toString() : "";
            
            // 更新当前交易记录的数据
            // 如果是负债记录，保持金额符号不变（通常是正数表示增加负债，负数表示减少负债）
            double updatedAmount = amount;
            if (mCurrentTransaction.getAmount() < 0) {
                updatedAmount = -amount; // 保持负号
            }
            
            mCurrentTransaction.setAmount(updatedAmount);
            mCurrentTransaction.setDescription(note);
            mCurrentTransaction.setDate(mSelectedDate);
            mCurrentTransaction.setPaymentTransactionNo(transactionNo);
            mCurrentTransaction.setDebtType(mSelectedDebtType);
            
            // 更新数据库
            mTransactionRepository.update(mCurrentTransaction);
            
            Toast.makeText(this, "负债记录已更新", Toast.LENGTH_SHORT).show();
            
            // 发送广播通知数据已更改
            Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
            sendBroadcast(intent);
            
            finish();
        }
    }

    private String getDebtTypeLabel(Transaction.DebtType debtType) {
        switch (debtType) {
            case HUABEI:
                return getString(R.string.huabei);
            case BAITIAO:
                return getString(R.string.baitiao);
            case CREDIT_CARD:
                return getString(R.string.credit_card);
            default:
                return "";
        }
    }

    private boolean validateInput() {
        boolean valid = true;
        
        if (TextUtils.isEmpty(mAmountEditText.getText())) {
            mAmountEditText.setError("请输入金额");
            valid = false;
        }
        
        return valid;
    }
} 