package com.example.jizhang;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;
import com.example.jizhang.ui.expense.AddExpenseActivity;
import com.example.jizhang.ui.income.AddIncomeActivity;
import com.example.jizhang.ui.settings.SettingsActivity;
import com.example.jizhang.ui.stats.StatsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private MainViewModel mViewModel;
    private TransactionAdapter mAdapter;
    private TextView mBalanceTextView;
    private TextView mIncomeTextView;
    private TextView mExpenseTextView;
    private TransactionRepository mTransactionRepository;
    
    // 存储权限请求码
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private static final int REQUEST_SETTINGS = 201;
    
    // 数据变更广播接收器
    private BroadcastReceiver mDataChangedReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 初始化视图
        mBalanceTextView = findViewById(R.id.balanceTextView);
        mIncomeTextView = findViewById(R.id.incomeTextView);
        mExpenseTextView = findViewById(R.id.expenseTextView);
        
        // 设置RecyclerView
        RecyclerView transactionsRecyclerView = findViewById(R.id.transactionsRecyclerView);
        mAdapter = new TransactionAdapter(this);
        transactionsRecyclerView.setAdapter(mAdapter);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 设置FloatingActionButton
        FloatingActionButton addExpenseButton = findViewById(R.id.addExpenseFab);
        addExpenseButton.setOnClickListener(view -> showAddExpenseOptions());
        
        FloatingActionButton addIncomeButton = findViewById(R.id.addIncomeFab);
        addIncomeButton.setOnClickListener(view -> showAddIncomeOptions());
        
        FloatingActionButton addDebtButton = findViewById(R.id.addDebtFab);
        addDebtButton.setOnClickListener(view -> showAddDebtOptions());

        // 设置BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);

        // 初始化TransactionRepository
        mTransactionRepository = new TransactionRepository(getApplication());
        
        // 初始化ViewModel并观察数据变化
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // 注册数据变更广播接收器
        registerDataChangedReceiver();
        
        // 请求存储权限
        checkAndRequestStoragePermissions();
    }
    
    /**
     * 注册数据变更广播接收器
     */
    private void registerDataChangedReceiver() {
        mDataChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.jizhang.DATA_CHANGED".equals(intent.getAction())) {
                    // 刷新数据
                    refreshData();
                }
            }
        };
        
        // 注册广播接收器
        IntentFilter filter = new IntentFilter("com.example.jizhang.DATA_CHANGED");
        registerReceiver(mDataChangedReceiver, filter);
    }
    
    /**
     * 刷新所有数据
     */
    private void refreshData() {
        // 刷新交易记录
        mTransactionRepository.getAllTransactions().removeObservers(this);
        mTransactionRepository.getAllTransactions().observe(this, transactions -> {
            if (transactions != null) {
                mAdapter.setTransactions(transactions);
            }
        });
        
        // 刷新收入
        mTransactionRepository.getTotalIncome().removeObservers(this);
        mTransactionRepository.getTotalIncome().observe(this, income -> {
            updateIncomeView(income != null ? income : 0);
        });

        // 刷新支出
        mTransactionRepository.getTotalExpense().removeObservers(this);
        mTransactionRepository.getTotalExpense().observe(this, expense -> {
            updateExpenseView(expense != null ? expense : 0);
        });
        
        // 刷新负债
        mTransactionRepository.getTotalDebt().removeObservers(this);
        mTransactionRepository.getTotalDebt().observe(this, debt -> {
            if (debt != null) {
                TextView debtTextView = findViewById(R.id.debtTextView);
                if (debtTextView != null) {
                    debtTextView.setText(formatCurrency(debt));
                }
            } else {
                TextView debtTextView = findViewById(R.id.debtTextView);
                if (debtTextView != null) {
                    debtTextView.setText(formatCurrency(0));
                }
            }
        });
        
        // 刷新余额
        mTransactionRepository.getActualBalance().removeObservers(this);
        mTransactionRepository.getActualBalance().observe(this, balance -> {
            if (balance != null) {
                mBalanceTextView.setText(formatCurrency(balance));
            } else {
                mBalanceTextView.setText(formatCurrency(0));
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消注册广播接收器
        if (mDataChangedReceiver != null) {
            unregisterReceiver(mDataChangedReceiver);
            mDataChangedReceiver = null;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK) {
            // 从设置界面返回，且有数据变更
            refreshData();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            // 启动设置界面，并期望返回结果
            startActivityForResult(new Intent(this, SettingsActivity.class), REQUEST_SETTINGS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 检查并请求存储权限
     */
    private void checkAndRequestStoragePermissions() {
        // 对于Android 10以下版本，需要传统的存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // 检查传统存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                
                // 如果权限未被授予，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        },
                        REQUEST_STORAGE_PERMISSION);
                
                // 显示权限用途提示
                Toast.makeText(this, "记账应用需要存储权限来导出数据", Toast.LENGTH_LONG).show();
            }
        } 
        // 对于Android 10及以上版本，我们使用了requestLegacyExternalStorage，但也请求权限
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                 Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                
                // 显示权限用途提示
                Toast.makeText(this, "记账应用需要存储权限来导出数据", Toast.LENGTH_LONG).show();
            }
        }
        // 对于Android 11及以上，可能需要使用存储访问框架或管理所有文件的权限
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上版本，如果需要广泛的存储访问，需要使用ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    
                    // 显示权限用途提示
                    Toast.makeText(this, "记账应用需要文件管理权限来导出数据，请在设置中授予权限", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    
                    // 显示权限用途提示
                    Toast.makeText(this, "请在设置中手动授予记账应用文件访问权限", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            }
        }
        // Android 13及以上版本，使用READ_MEDIA_*权限
        else if (Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU
            boolean hasReadMediaImages = ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
            boolean hasReadMediaVideo = ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
            
            if (!hasReadMediaImages || !hasReadMediaVideo) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO
                        },
                        REQUEST_STORAGE_PERMISSION);
                
                // 显示权限用途提示
                Toast.makeText(this, "记账应用需要媒体访问权限来保存导出的数据", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "已获得存储权限，可以使用导出功能", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "未获得所需权限，导出功能可能无法正常工作", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.navigation_home) {
            return true;
        } else if (itemId == R.id.navigation_expense_details) {
            startActivity(new Intent(this, com.example.jizhang.ui.expense.ExpenseDetailsActivity.class));
            return true;
        } else if (itemId == R.id.navigation_income_details) {
            startActivity(new Intent(this, com.example.jizhang.ui.income.IncomeDetailsActivity.class));
            return true;
        } else if (itemId == R.id.navigation_stats) {
            startActivity(new Intent(this, StatsActivity.class));
            return true;
        } else if (itemId == R.id.navigation_debt_details) {
            startActivity(new Intent(this, com.example.jizhang.ui.debt.DebtDetailsActivity.class));
            return true;
        }
        
        return false;
    }

    private void showAddExpenseOptions() {
        // 打开添加支出界面
        startActivity(new Intent(this, AddExpenseActivity.class));
    }
    
    private void showAddIncomeOptions() {
        // 打开添加收入界面
        startActivity(new Intent(this, AddIncomeActivity.class));
    }

    private void showAddDebtOptions() {
        Intent intent = new Intent(this, com.example.jizhang.ui.debt.AddDebtActivity.class);
        startActivity(intent);
    }

    private void updateBalanceView(double balance) {
        mBalanceTextView.setText(formatCurrency(balance));
    }

    private void updateIncomeView(double income) {
        mIncomeTextView.setText(formatCurrency(income));
    }

    private void updateExpenseView(double expense) {
        mExpenseTextView.setText(formatCurrency(expense));
    }
    
    /**
     * 将数字格式化为货币格式
     * @param amount 要格式化的金额
     * @return 格式化后的货币字符串
     */
    private String formatCurrency(double amount) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        return currencyFormat.format(amount);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 每次恢复活动时刷新数据
        refreshData();
    }
} 