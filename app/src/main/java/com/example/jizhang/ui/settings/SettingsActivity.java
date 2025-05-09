package com.example.jizhang.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.jizhang.JizhangApplication;
import com.example.jizhang.R;
import com.example.jizhang.model.CategoryRepository;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;
import com.example.jizhang.model.TransactionDao;
import com.example.jizhang.model.AppDatabase;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    
    private CategoryRepository mCategoryRepository;
    
    private Toolbar mToolbar;
    private Switch mSwitchDarkMode;
    private Button mBtnExportData;
    private Button mBtnExportSql;
    private Button mBtnImportData;
    private Button mBtnGetTemplate;
    private Button mBtnClearData;
    private Button mBtnManageCategories;
    
    private TransactionRepository mTransactionRepository;
    
    // 添加请求码常量
    private static final int REQUEST_WRITE_STORAGE = 101;
    private static final int REQUEST_FILE_IMPORT = 102;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        // 初始化Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 初始化UI组件
        mSwitchDarkMode = findViewById(R.id.darkModeSwitch);
        mBtnExportData = findViewById(R.id.exportDataButton);
        mBtnExportSql = findViewById(R.id.exportSqlButton);
        mBtnImportData = findViewById(R.id.importDataButton);
        mBtnGetTemplate = findViewById(R.id.getTemplateButton);
        mBtnClearData = findViewById(R.id.clearDataButton);
        mBtnManageCategories = findViewById(R.id.manageCategoriesButton);
        
        // 获取Repository实例
        mCategoryRepository = new CategoryRepository(getApplication());
        mTransactionRepository = new TransactionRepository(getApplication());
        
        // 设置按钮点击监听器
        mBtnExportData.setOnClickListener(this);
        mBtnExportSql.setOnClickListener(this);
        mBtnImportData.setOnClickListener(this);
        mBtnGetTemplate.setOnClickListener(this);
        mBtnClearData.setOnClickListener(this);
        mBtnManageCategories.setOnClickListener(this);
        
        // 根据开关标志显示或隐藏导出SQL按钮
        if (JizhangApplication.ENABLE_EXPORT_SQL) {
            mBtnExportSql.setVisibility(View.VISIBLE);
        } else {
            mBtnExportSql.setVisibility(View.GONE);
        }
        
        // 加载暗黑模式设置
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        mSwitchDarkMode.setChecked(isDarkMode);
        
        // 设置暗黑模式切换监听器
        mSwitchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存设置
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("DarkMode", isChecked);
            editor.apply();
            
            // 应用主题
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            
            // 重启Activity应用主题
            recreate();
        });
        
        // 主动请求存储权限
        checkAndRequestStoragePermissions();
    }
    
    /**
     * 检查并请求存储权限
     */
    private void checkAndRequestStoragePermissions() {
        // 对于Android 10以下版本，需要传统的存储权限
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
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
                        REQUEST_WRITE_STORAGE);
            }
        } 
        // 对于Android 10及以上版本，我们使用了requestLegacyExternalStorage，但也请求权限
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q &&
                 android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
        }
        // 对于Android 11及以上，可能需要使用存储访问框架或管理所有文件的权限
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上版本，如果需要广泛的存储访问，需要使用ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    Toast.makeText(this, "需要允许管理所有文件权限才能导出数据", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    Toast.makeText(this, "请手动授予存储权限", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                }
            }
        }
        // Android 13及以上版本，使用READ_MEDIA_*权限
        else if (android.os.Build.VERSION.SDK_INT >= 33) { // Build.VERSION_CODES.TIRAMISU
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
                        REQUEST_WRITE_STORAGE);
            }
        }
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
    public void onClick(View v) {
        int id = v.getId();
        
        if (id == R.id.exportDataButton) {
            exportData();
        } else if (id == R.id.exportSqlButton) {
            exportSqlFile();
        } else if (id == R.id.importDataButton) {
            importData();
        } else if (id == R.id.getTemplateButton) {
            createTemplateFile();
        } else if (id == R.id.clearDataButton) {
            showClearDataConfirmationDialog();
        } else if (id == R.id.manageCategoriesButton) {
            manageCategories();
        }
    }
    
    /**
     * 导出数据
     */
    private void exportData() {
        // 检查权限
        boolean hasPermission = false;
        
        // 根据Android版本检查不同的权限
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Android 10以下，检查传统存储权限
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上，检查MANAGE_EXTERNAL_STORAGE权限
            hasPermission = android.os.Environment.isExternalStorageManager();
        } else {
            // Android 10，使用requestLegacyExternalStorage属性
            hasPermission = true;
        }
        
        if (!hasPermission) {
            // 提示用户权限的重要性
            Toast.makeText(this, "导出数据需要存储权限，请授予权限", Toast.LENGTH_LONG).show();
            
            // 如果权限未被授予，请求权限
            checkAndRequestStoragePermissions();
            return;
        }
        
        // 执行导出
        performExport();
    }
    
    /**
     * 执行数据导出
     */
    private void performExport() {
        // 创建一个单线程执行器来执行导出操作
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 创建保存目录
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "记账应用");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                // 创建包含时间戳的文件名
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timestamp = sdf.format(new java.util.Date());
                File file = new File(exportDir, "记账数据_" + timestamp + ".csv");
                
                // 创建带BOM的UTF-8文件输出流
                OutputStream outputStream = new java.io.FileOutputStream(file);
                // 写入UTF-8 BOM标识
                outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                CSVWriter csvWriter = new CSVWriter(writer);
                
                // 写入标题行
                String[] headerRecord = {"交易ID", "金额", "描述", "日期", "类别", "类型", "支付平台交易单号", "负债类型"};
                csvWriter.writeNext(headerRecord);
                
                // 获取所有交易记录
                TransactionDao transactionDao = AppDatabase.getDatabase(getApplication()).transactionDao();
                List<Transaction> transactions = transactionDao.getAllTransactionsSync();
                
                // 写入交易数据
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                for (Transaction transaction : transactions) {
                    // 根据交易类型确定显示文本
                    String type;
                    switch (transaction.getType()) {
                        case EXPENSE:
                            type = "支出";
                            break;
                        case INCOME:
                            type = "收入";
                            break;
                        case DEBT:
                            type = "负债";
                            break;
                        default:
                            type = "未知";
                    }
                    
                    // 获取负债类型文本
                    String debtTypeStr = "";
                    if (transaction.getType() == Transaction.TransactionType.DEBT && transaction.getDebtType() != null) {
                        switch (transaction.getDebtType()) {
                            case HUABEI:
                                debtTypeStr = "花呗";
                                break;
                            case BAITIAO:
                                debtTypeStr = "白条";
                                break;
                            case CREDIT_CARD:
                                debtTypeStr = "信用卡";
                                break;
                            default:
                                debtTypeStr = "无";
                        }
                    }
                    
                    String[] dataRecord = {
                            String.valueOf(transaction.getId()),
                            String.valueOf(transaction.getAmount()),
                            transaction.getDescription(),
                            dateFormat.format(transaction.getDate()),
                            transaction.getCategory(),
                            type,
                            transaction.getPaymentTransactionNo(),
                            debtTypeStr
                    };
                    csvWriter.writeNext(dataRecord);
                }
                
                // 关闭CSV写入器
                csvWriter.close();
                
                // 在UI线程显示成功消息
                runOnUiThread(() -> {
                    Toast.makeText(this, "数据已导出到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    
                    // 尝试分享文件
                    try {
                        // 创建分享意图
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/csv");
                        
                        // 验证文件是否存在
                        if (!file.exists()) {
                            Toast.makeText(this, "导出文件不存在: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // 使用FileProvider获取文件URI
                        Uri fileUri = FileProvider.getUriForFile(
                                this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                file);
                        
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // 启动分享
                        startActivity(Intent.createChooser(shareIntent, "分享导出数据"));
                    } catch (IllegalArgumentException e) {
                        // FileProvider.getUriForFile可能会抛出IllegalArgumentException
                        Log.e("SettingsActivity", "分享文件失败: " + e.getMessage(), e);
                        Toast.makeText(this, "无法分享文件，但文件已成功导出到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // 捕获其他可能的异常
                        Log.e("SettingsActivity", "分享时出现异常: " + e.getMessage(), e);
                        Toast.makeText(this, "分享过程出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "导出数据失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 导入数据
     */
    private void importData() {
        // 检查权限
        boolean hasPermission = false;
        
        // 根据Android版本检查不同的权限
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Android 10以下，检查传统存储权限
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上，检查MANAGE_EXTERNAL_STORAGE权限
            hasPermission = android.os.Environment.isExternalStorageManager();
        } else {
            // Android 10，使用requestLegacyExternalStorage属性
            hasPermission = true;
        }
        
        if (!hasPermission) {
            // 提示用户权限的重要性
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            
            // 如果权限未被授予，请求权限
            checkAndRequestStoragePermissions();
            return;
        }
        
        // 创建文件选择器意图
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // 修改MIME类型设置，使用更通用的类型
        intent.setType("*/*");
        // 添加额外的MIME类型过滤器
        String[] mimeTypes = {"text/csv", "text/comma-separated-values", "application/csv", "application/vnd.ms-excel"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_csv_file)), REQUEST_FILE_IMPORT);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.install_file_manager, Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_FILE_IMPORT && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                
                // 检查文件类型
                String fileName = getFileNameFromUri(uri);
                if (fileName != null && !fileName.toLowerCase().endsWith(".csv")) {
                    Toast.makeText(this, "请选择CSV文件（.csv后缀）", Toast.LENGTH_LONG).show();
                    return;
                }
                
                importCSV(uri);
            }
        }
    }
    
    /**
     * 从URI获取文件名
     * @param uri 文件URI
     * @return 文件名
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    
    /**
     * 从URI导入CSV文件
     * @param uri CSV文件的URI
     */
    private void importCSV(Uri uri) {
        // 显示进度对话框
        androidx.appcompat.app.AlertDialog progressDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("导入中")
                .setMessage("正在导入数据，请稍候...")
                .setCancelable(false)
                .show();
            
        // 使用单线程执行导入操作
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 获取输入流
                java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.file_read_error, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // 检测BOM头并处理
                java.io.PushbackInputStream pushbackInputStream = new java.io.PushbackInputStream(inputStream, 3);
                byte[] bom = new byte[3];
                boolean hasBOM = false;
                
                // 尝试读取前3个字节检查BOM
                if (pushbackInputStream.read(bom) == 3) {
                    // 检查UTF-8 BOM (EF BB BF)
                    if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
                        hasBOM = true;
                    } else {
                        // 如果不是BOM，把读取的字节放回流中
                        pushbackInputStream.unread(bom);
                    }
                } else {
                    // 文件太短，放回已读取的字节
                    pushbackInputStream.unread(bom, 0, pushbackInputStream.available());
                }
                
                // 使用适当的编码读取CSV
                java.io.InputStreamReader reader;
                if (hasBOM) {
                    reader = new java.io.InputStreamReader(pushbackInputStream, "UTF-8");
                } else {
                    // 尝试使用系统默认编码，如果有问题可以尝试其他编码
                    reader = new java.io.InputStreamReader(pushbackInputStream);
                }
                
                // 创建CSVReader，设置更灵活的分隔符检测
                com.opencsv.CSVReaderBuilder readerBuilder = new com.opencsv.CSVReaderBuilder(reader);
                com.opencsv.CSVReader csvReader = readerBuilder.build();
                
                // 获取TransactionDao进行数据操作
                TransactionDao transactionDao = AppDatabase.getDatabase(getApplication()).transactionDao();
                
                // 读取第一行作为标题
                String[] headerRecord = csvReader.readNext();
                if (headerRecord == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.file_format_error, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // 检查文件格式
                if (headerRecord.length < 6) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, R.string.csv_format_error, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // 读取并导入每一行数据
                String[] nextRecord;
                int importCount = 0;
                int errorCount = 0;
                SimpleDateFormat dateFormatDash = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat dateFormatSlash = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                
                // 创建一个列表来存储错误信息
                java.util.List<String> errorDetails = new java.util.ArrayList<>();
                
                while ((nextRecord = csvReader.readNext()) != null) {
                    try {
                        // 检查是否为空行或所有列都为空
                        boolean isEmptyRow = true;
                        if (nextRecord.length > 0) {
                            for (String field : nextRecord) {
                                if (field != null && !field.trim().isEmpty()) {
                                    isEmptyRow = false;
                                    break;
                                }
                            }
                        }
                        
                        // 如果是空行或所有列都为空，直接跳过不处理，也不计入错误
                        if (isEmptyRow) {
                            continue;
                        }
                        
                        // 确保行数据长度足够
                        if (nextRecord.length < 6) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 列数不足，至少需要6列");
                            continue;
                        }
                        
                        // 检查金额字段是否为空
                        if (nextRecord[1] == null || nextRecord[1].trim().isEmpty()) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 金额不能为空");
                            continue;
                        }
                        
                        // 解析金额
                        double amount;
                        try {
                            amount = Double.parseDouble(nextRecord[1]);
                        } catch (NumberFormatException e) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 金额格式不正确 - " + nextRecord[1]);
                            continue;
                        }
                        
                        // 检查描述字段
                        String description = nextRecord[2] != null ? nextRecord[2].trim() : "";
                        
                        // 检查日期字段是否为空
                        if (nextRecord[3] == null || nextRecord[3].trim().isEmpty()) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 日期不能为空");
                            continue;
                        }
                        
                        // 解析日期（尝试两种格式）
                        Date date = null;
                        String dateString = nextRecord[3].trim();
                        try {
                            // 先尝试使用连字符格式解析
                            date = dateFormatDash.parse(dateString);
                            
                            // 如果连字符格式解析失败，尝试使用斜杠格式
                            if (date == null) {
                                date = dateFormatSlash.parse(dateString);
                            }
                            
                            // 如果两种格式都解析失败，报错
                            if (date == null) {
                                errorCount++;
                                errorDetails.add("第" + (importCount + errorCount) + "行: 日期格式不正确 - " + dateString);
                                continue;
                            }
                        } catch (java.text.ParseException e) {
                            try {
                                // 尝试使用斜杠格式
                                date = dateFormatSlash.parse(dateString);
                                if (date == null) {
                                    throw e; // 如果还是null，抛出异常
                                }
                            } catch (java.text.ParseException ex) {
                                errorCount++;
                                errorDetails.add("第" + (importCount + errorCount) + "行: 日期格式不正确 - " + dateString + "，请使用yyyy-MM-dd或yyyy/MM/dd格式");
                                continue;
                            }
                        }
                        
                        // 检查类别字段是否为空
                        if (nextRecord[4] == null || nextRecord[4].trim().isEmpty()) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 类别不能为空");
                            continue;
                        }
                        
                        // 解析类别
                        String category = nextRecord[4].trim();
                        
                        // 检查交易类型字段是否为空
                        if (nextRecord[5] == null || nextRecord[5].trim().isEmpty()) {
                            errorCount++;
                            errorDetails.add("第" + (importCount + errorCount) + "行: 交易类型不能为空");
                            continue;
                        }
                        
                        // 解析交易类型
                        Transaction.TransactionType type;
                        switch (nextRecord[5].trim()) {
                            case "收入":
                                type = Transaction.TransactionType.INCOME;
                                break;
                            case "支出":
                                type = Transaction.TransactionType.EXPENSE;
                                break;
                            case "负债":
                                type = Transaction.TransactionType.DEBT;
                                break;
                            default:
                                errorCount++;
                                errorDetails.add("第" + (importCount + errorCount) + "行: 交易类型不正确 - " + nextRecord[5]);
                                continue; // 跳过不认识的交易类型
                        }
                        
                        // 解析支付平台交易单号（如果有）
                        String paymentTransactionNo = "";
                        if (nextRecord.length > 6 && nextRecord[6] != null && !nextRecord[6].trim().isEmpty()) {
                            paymentTransactionNo = nextRecord[6].trim();
                        }
                        
                        // 解析负债类型（如果有）
                        Transaction.DebtType debtType = Transaction.DebtType.NONE;
                        if (type == Transaction.TransactionType.DEBT && nextRecord.length > 7 && nextRecord[7] != null && !nextRecord[7].trim().isEmpty()) {
                            switch (nextRecord[7].trim()) {
                                case "花呗":
                                    debtType = Transaction.DebtType.HUABEI;
                                    break;
                                case "白条":
                                    debtType = Transaction.DebtType.BAITIAO;
                                    break;
                                case "信用卡":
                                    debtType = Transaction.DebtType.CREDIT_CARD;
                                    break;
                            }
                        }
                        
                        // 创建交易记录对象（不包括ID，ID会由Room自动生成）
                        Transaction transaction = new Transaction(
                                0, // ID为0，由Room自动生成
                                amount,
                                description,
                                date,
                                category,
                                type,
                                paymentTransactionNo,
                                debtType
                        );
                        
                        // 插入数据库
                        transactionDao.insert(transaction);
                        
                        importCount++;
                    } catch (Exception e) {
                        // 跳过格式不正确的行
                        errorCount++;
                        errorDetails.add("第" + (importCount + errorCount) + "行: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // 关闭CSV读取器
                csvReader.close();
                
                // 显示导入结果
                final int finalImportCount = importCount;
                final int finalErrorCount = errorCount;
                final java.util.List<String> finalErrorDetails = errorDetails;
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    String message = getString(R.string.import_success, finalImportCount);
                    
                    // 如果有导入失败的记录，显示错误详情对话框
                    if (finalErrorCount > 0) {
                        // 先显示简短的成功消息
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        
                        // 构建错误详情字符串
                        StringBuilder errorBuilder = new StringBuilder();
                        errorBuilder.append("成功导入 ").append(finalImportCount).append(" 条记录，")
                                   .append(finalErrorCount).append(" 条记录导入失败。\n\n");
                        
                        // 添加关于空行的特别说明
                        errorBuilder.append("注意：完全空白的行或所有列都为空的行已被自动跳过，不计入错误。\n\n");
                        
                        errorBuilder.append("错误详情：\n");
                        
                        // 最多显示前10条错误信息，避免对话框过长
                        int displayCount = Math.min(finalErrorDetails.size(), 10);
                        for (int i = 0; i < displayCount; i++) {
                            errorBuilder.append(finalErrorDetails.get(i)).append("\n");
                        }
                        
                        // 如果错误超过10条，显示省略提示
                        if (finalErrorDetails.size() > 10) {
                            errorBuilder.append("...(还有 ").append(finalErrorDetails.size() - 10).append(" 条错误未显示)");
                        }
                        
                        // 显示错误详情对话框
                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                .setTitle("导入结果")
                                .setMessage(errorBuilder.toString())
                                .setPositiveButton("确定", null)
                                .setNeutralButton("导出错误日志", (dialog, which) -> exportErrorLog(finalErrorDetails))
                                .show();
                    } else {
                        // 没有错误时，显示成功消息
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                    
                    // 显示对话框询问是否需要导出当前所有记录作为备份
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("导入完成")
                            .setMessage("是否需要导出当前所有记录作为备份？")
                            .setPositiveButton("是", (dialog, which) -> exportData())
                            .setNegativeButton("否", null)
                            .show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                final String errorMessage = e.getMessage();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, getString(R.string.import_failed, errorMessage), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            // 检查是否所有权限都被授予
            boolean allPermissionsGranted = true;
            if (grantResults.length > 0) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
            } else {
                allPermissionsGranted = false;
            }
            
            if (allPermissionsGranted) {
                // 权限获取成功，执行导出
                performExport();
            } else {
                Toast.makeText(this, "需要存储权限才能导出数据", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * 创建CSV模板文件
     */
    private void createTemplateFile() {
        // 检查存储权限
        boolean hasPermission = false;
        
        // 根据Android版本检查不同的权限
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Android 10以下，检查传统存储权限
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上，检查MANAGE_EXTERNAL_STORAGE权限
            hasPermission = android.os.Environment.isExternalStorageManager();
        } else {
            // Android 10，使用requestLegacyExternalStorage属性
            hasPermission = true;
        }
        
        if (!hasPermission) {
            // 提示用户权限的重要性
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_LONG).show();
            
            // 如果权限未被授予，请求权限
            checkAndRequestStoragePermissions();
            return;
        }
        
        // 显示进度对话框
        androidx.appcompat.app.AlertDialog progressDialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("模板文件")
                .setMessage(R.string.creating_template)
                .setCancelable(false)
                .show();
        
        // 使用单线程执行创建模板文件操作
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 创建保存目录
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "记账应用");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                // 创建模板文件名
                File file = new File(exportDir, "记账模板.csv");
                
                // 创建带BOM的UTF-8文件输出流
                java.io.OutputStream outputStream = new java.io.FileOutputStream(file);
                // 写入UTF-8 BOM标识
                outputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
                java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(outputStream, "UTF-8");
                CSVWriter csvWriter = new CSVWriter(writer);
                
                // 写入标题行
                String[] headerRecord = {"交易ID", "金额", "描述", "日期", "类别", "类型", "支付平台交易单号", "负债类型"};
                csvWriter.writeNext(headerRecord);
                
                // 写入示例数据
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String today = dateFormat.format(new Date());
                
                // 收入示例
                String[] incomeExample = {"", "5000", "工资收入", today, "工资", "收入", "", ""};
                csvWriter.writeNext(incomeExample);
                
                // 支出示例
                String[] expenseExample = {"", "100", "午餐", today, "饮食", "支出", "", ""};
                csvWriter.writeNext(expenseExample);
                
                // 负债示例
                String[] debtExample = {"", "2000", "购物", today, "购物", "负债", "", "花呗"};
                csvWriter.writeNext(debtExample);
                
                // 关闭CSV写入器
                csvWriter.close();
                
                // 更新UI
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    // 显示成功消息
                    Toast.makeText(this, getString(R.string.template_created, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
                    
                    // 尝试分享文件
                    try {
                        // 验证文件是否存在
                        if (!file.exists()) {
                            Toast.makeText(this, "导出文件不存在: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // 创建分享意图
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/csv");
                        
                        // 使用FileProvider获取文件URI
                        Uri fileUri = FileProvider.getUriForFile(
                                this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                file);
                        
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // 启动分享
                        startActivity(Intent.createChooser(shareIntent, "分享模板文件"));
                    } catch (IllegalArgumentException e) {
                        // FileProvider.getUriForFile可能会抛出IllegalArgumentException
                        Log.e("SettingsActivity", "分享模板文件失败: " + e.getMessage(), e);
                        Toast.makeText(this, "无法分享文件，但模板文件已成功创建到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // 捕获其他可能的异常
                        Log.e("SettingsActivity", "分享模板时出现异常: " + e.getMessage(), e);
                        Toast.makeText(this, "分享过程出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, getString(R.string.template_error, e.getMessage()), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * 显示清除所有数据的确认对话框
     */
    private void showClearDataConfirmationDialog() {
        String[] options = {"清除所有数据（包括自定义类别）", "仅清除交易记录", "仅清除自定义类别"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("选择清除数据选项");
        builder.setSingleChoiceItems(options, 0, null);
        builder.setPositiveButton("确定", (dialog, which) -> {
            int selectedPosition = ((androidx.appcompat.app.AlertDialog) dialog).getListView().getCheckedItemPosition();
            switch (selectedPosition) {
                case 0:
                    showFinalConfirmationDialog("确定要清除所有数据吗？此操作将删除所有交易记录和自定义类别，不可恢复！", () -> clearAllData(true, true));
                    break;
                case 1:
                    showFinalConfirmationDialog("确定要清除所有交易记录吗？此操作不可恢复！", () -> clearAllData(true, false));
                    break;
                case 2:
                    showFinalConfirmationDialog("确定要清除所有自定义类别吗？此操作不可恢复！", () -> clearAllData(false, true));
                    break;
            }
        });
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    /**
     * 显示最终确认对话框
     */
    private void showFinalConfirmationDialog(String message, Runnable onConfirm) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("确认操作");
        builder.setMessage(message);
        builder.setPositiveButton("确定", (dialog, which) -> onConfirm.run());
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    
    /**
     * 清除数据
     * @param clearTransactions 是否清除交易记录
     * @param clearCategories 是否清除自定义类别
     */
    private void clearAllData(boolean clearTransactions, boolean clearCategories) {
        // 显示进度对话框
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("正在清除数据...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        // 使用异步任务清除数据
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 获取数据库实例
                AppDatabase db = AppDatabase.getDatabase(getApplication());
                
                // 清除交易记录
                if (clearTransactions) {
                    db.transactionDao().deleteAllTransactions();
                }
                
                // 清除自定义类别
                if (clearCategories) {
                    // 只删除自定义类别，保留默认类别
                    db.categoryDao().deleteCustomExpenseCategories();
                    db.categoryDao().deleteCustomIncomeCategories();
                }
                
                // 在主线程更新UI
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    
                    String message;
                    if (clearTransactions && clearCategories) {
                        message = "所有数据已清除";
                    } else if (clearTransactions) {
                        message = "所有交易记录已清除";
                    } else {
                        message = "所有自定义类别已清除";
                    }
                    
                    Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    // 通知数据已更改
                    notifyMainActivityDataChanged();
                });
            } catch (Exception e) {
                // 处理异常
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(SettingsActivity.this, "清除数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * 通知主界面数据已更改
     */
    private void notifyMainActivityDataChanged() {
        // 方法1: 发送广播通知MainActivity刷新数据
        Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
        sendBroadcast(intent);
        
        // 方法2: 设置结果，让MainActivity知道需要刷新数据
        setResult(RESULT_OK);
        
        // 完成设置界面的任务
        finish();
    }
    
    /**
     * 打开类别管理界面
     */
    private void manageCategories() {
        Intent intent = new Intent(this, CategoryManagementActivity.class);
        startActivity(intent);
    }
    
    /**
     * 导出SQL文件
     */
    private void exportSqlFile() {
        // 检查存储权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Toast.makeText(this, "需要允许管理所有文件权限才能导出SQL文件", Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    android.net.Uri uri = android.net.Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
                return;
            }
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "需要存储权限才能导出SQL文件", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            return;
        }
        
        // 执行导出操作
        performSqlExport();
    }
    
    /**
     * 执行SQL文件导出
     */
    private void performSqlExport() {
        Toast.makeText(this, "正在导出SQL文件...", Toast.LENGTH_SHORT).show();
        
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 获取数据库文件
                File dbFile = getDatabasePath("jizhang_database");
                if (!dbFile.exists()) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, 
                            "数据库文件不存在", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                // 创建导出目录
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "JizhangExport");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                // 创建导出的SQL文件
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String timestamp = dateFormat.format(new Date());
                File exportFile = new File(exportDir, "jizhang_db_" + timestamp + ".db");
                
                // 复制数据库文件
                java.nio.channels.FileChannel source = null;
                java.nio.channels.FileChannel destination = null;
                try {
                    source = new java.io.FileInputStream(dbFile).getChannel();
                    destination = new java.io.FileOutputStream(exportFile).getChannel();
                    destination.transferFrom(source, 0, source.size());
                    
                    runOnUiThread(() -> {
                        Toast.makeText(SettingsActivity.this, 
                                "SQL文件已导出到: " + exportFile.getAbsolutePath(), 
                                Toast.LENGTH_LONG).show();
                    });
                } finally {
                    if (source != null) {
                        source.close();
                    }
                    if (destination != null) {
                        destination.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, 
                            "导出SQL文件失败: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 导出错误日志到文件
     * @param errorDetails 错误详情列表
     */
    private void exportErrorLog(java.util.List<String> errorDetails) {
        // 检查存储权限
        boolean hasPermission = false;
        
        // 根据Android版本检查不同的权限
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
            // Android 10以下，检查传统存储权限
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Android 11及以上，检查MANAGE_EXTERNAL_STORAGE权限
            hasPermission = android.os.Environment.isExternalStorageManager();
        } else {
            // Android 10，使用requestLegacyExternalStorage属性
            hasPermission = true;
        }
        
        if (!hasPermission) {
            // 提示用户权限的重要性
            Toast.makeText(this, "导出错误日志需要存储权限，请授予权限", Toast.LENGTH_LONG).show();
            
            // 如果权限未被授予，请求权限
            checkAndRequestStoragePermissions();
            return;
        }
        
        // 使用单线程执行导出操作
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 创建保存目录
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "记账应用");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                // 创建错误日志文件名（使用当前时间戳）
                SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String fileName = "导入错误_" + fileNameFormat.format(new Date()) + ".txt";
                File file = new File(exportDir, fileName);
                
                // 创建文件写入器
                FileWriter writer = new FileWriter(file);
                
                // 写入错误详情
                writer.write("记账应用导入错误日志\n");
                writer.write("生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\n\n");
                
                for (String error : errorDetails) {
                    writer.write(error + "\n");
                }
                
                // 关闭写入器
                writer.close();
                
                // 更新UI
                runOnUiThread(() -> {
                    // 显示成功消息
                    Toast.makeText(this, "错误日志已保存至: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    
                    // 尝试分享文件
                    try {
                        // 验证文件是否存在
                        if (!file.exists()) {
                            Toast.makeText(this, "日志文件不存在: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        
                        // 创建分享意图
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        
                        // 使用FileProvider获取文件URI
                        Uri fileUri = FileProvider.getUriForFile(
                                this,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                file);
                        
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        
                        // 启动分享
                        startActivity(Intent.createChooser(shareIntent, "分享错误日志"));
                    } catch (IllegalArgumentException e) {
                        // FileProvider.getUriForFile可能会抛出IllegalArgumentException
                        Log.e("SettingsActivity", "分享错误日志失败: " + e.getMessage(), e);
                        Toast.makeText(this, "无法分享错误日志，但日志已成功保存到: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // 捕获其他可能的异常
                        Log.e("SettingsActivity", "分享错误日志时出现异常: " + e.getMessage(), e);
                        Toast.makeText(this, "分享过程出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "导出错误日志失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
} 