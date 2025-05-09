package com.example.jizhang.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.Category;
import com.example.jizhang.model.CategorySummary;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsActivity extends AppCompatActivity {
    
    private TransactionRepository mTransactionRepository;
    private BarChart mBarChart;
    private PieChart mIncomePieChart;
    private PieChart mExpensePieChart;
    private PieChart mDebtPieChart;
    private RecyclerView mCategorySummaryRecyclerView;
    private CategorySummaryAdapter mCategorySummaryAdapter;
    private TabLayout mTabLayout;
    
    // 日期范围类型
    private enum DateRangeType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    private DateRangeType mCurrentDateRange = DateRangeType.DAILY;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // 初始化TransactionRepository
        mTransactionRepository = new TransactionRepository(getApplication());
        
        // 初始化视图
        mBarChart = findViewById(R.id.barChart);
        mIncomePieChart = findViewById(R.id.incomePieChart);
        mExpensePieChart = findViewById(R.id.expensePieChart);
        mDebtPieChart = findViewById(R.id.debtPieChart);
        mCategorySummaryRecyclerView = findViewById(R.id.categorySummaryRecyclerView);
        mTabLayout = findViewById(R.id.tabLayout);
        
        // 设置CategorySummaryRecyclerView
        mCategorySummaryAdapter = new CategorySummaryAdapter(this);
        mCategorySummaryRecyclerView.setAdapter(mCategorySummaryAdapter);
        mCategorySummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // 初始化图表
        setupBarChart();
        setupPieCharts();
        
        // 设置TabLayout监听器
        mTabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
                // 根据选中的Tab更新日期范围
                switch (tab.getPosition()) {
                    case 0:
                        mCurrentDateRange = DateRangeType.DAILY;
                        break;
                    case 1:
                        mCurrentDateRange = DateRangeType.WEEKLY;
                        break;
                    case 2:
                        mCurrentDateRange = DateRangeType.MONTHLY;
                        break;
                    case 3:
                        mCurrentDateRange = DateRangeType.YEARLY;
                        break;
                }
                
                // 清空之前的数据
                clearCharts();
                
                // 加载当前选中标签页的数据
                loadData();
            }
            
            @Override
            public void onTabUnselected(Tab tab) {
                // 不需要实现
            }
            
            @Override
            public void onTabReselected(Tab tab) {
                // 重新加载当前标签页的数据
                clearCharts();
                loadData();
            }
        });
        
        // 确保mTabLayout已经设置了标签页并且已正确初始化
        if (mTabLayout.getTabCount() > 0) {
            // 首先手动设置mCurrentDateRange
            mCurrentDateRange = DateRangeType.DAILY;
            // 然后选中第一个标签页（避免触发二次加载）
            mTabLayout.getTabAt(0).select();
        } else {
            // 如果没有标签页，则手动加载每日统计数据
            mCurrentDateRange = DateRangeType.DAILY;
            loadData();
        }
    }
    
    /**
     * 清空所有图表数据
     */
    private void clearCharts() {
        if (mBarChart != null) {
            mBarChart.clear();
            mBarChart.invalidate();
        }
        if (mIncomePieChart != null) {
            mIncomePieChart.clear();
            mIncomePieChart.invalidate();
        }
        if (mExpensePieChart != null) {
            mExpensePieChart.clear();
            mExpensePieChart.invalidate();
        }
        if (mDebtPieChart != null) {
            mDebtPieChart.clear();
            mDebtPieChart.invalidate();
        }
    }
    
    /**
     * 设置柱状图
     */
    private void setupBarChart() {
        mBarChart.getDescription().setEnabled(false);
        mBarChart.setDrawGridBackground(false);
        mBarChart.setDrawBarShadow(false);
        mBarChart.setHighlightFullBarEnabled(false);
        
        // 设置X轴
        XAxis xAxis = mBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        
        // 设置左侧Y轴
        YAxis leftAxis = mBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        
        // 禁用右侧Y轴
        mBarChart.getAxisRight().setEnabled(false);
        
        // 设置图例
        Legend legend = mBarChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        
        // 启用缩放和拖动
        mBarChart.setScaleEnabled(false);
        mBarChart.setDragEnabled(true);
        mBarChart.setTouchEnabled(true);
        
        // 动画
        mBarChart.animateY(1500);
    }
    
    /**
     * 设置饼图
     */
    private void setupPieCharts() {
        // 设置收入饼图
        setupPieChart(mIncomePieChart, "收入类别占比");
        
        // 设置支出饼图
        setupPieChart(mExpensePieChart, "支出类别占比");
        
        // 设置负债饼图
        setupPieChart(mDebtPieChart, "负债类别占比");
    }
    
    /**
     * 设置单个饼图的样式
     */
    private void setupPieChart(PieChart pieChart, String description) {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setCenterText(description);
        pieChart.setCenterTextSize(16f);
        
        // 设置图例
        Legend legend = pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        
        // 动画
        pieChart.animateY(1500);
    }
    
    /**
     * 加载数据
     */
    private void loadData() {
        // 根据当前选择的日期范围获取交易数据
        Calendar calendar = Calendar.getInstance(); // 当前时间，作为结束日期
        Calendar startDate = Calendar.getInstance(); // 起始日期
        
        // 重置日历对象，确保时间基准正确
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        
        // 设置结束时间为当天的23:59:59.999
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        
        // 根据不同的日期范围类型设置起始日期
        switch (mCurrentDateRange) {
            case DAILY:
                // 当天不需要调整startDate，因为已经设置为今天的0点
                break;
            case WEEKLY:
                // 过去7天
                startDate.add(Calendar.DAY_OF_MONTH, -6); // 包括今天共7天
                break;
            case MONTHLY:
                // 过去30天
                startDate.add(Calendar.DAY_OF_MONTH, -29); // 包括今天共30天
                break;
            case YEARLY:
                // 过去365天
                startDate.add(Calendar.DAY_OF_MONTH, -364); // 包括今天共365天
                break;
        }
        
        // 获取指定时间范围内的交易数据 - 使用按天比较的方法
        mTransactionRepository.getTransactionsBetweenDatesByDay(startDate.getTime(), calendar.getTime())
                .observe(this, transactions -> {
                    if (transactions != null) {
                        // 更新UI和图表
                        updateBarChart(transactions);
                        updatePieChart(mIncomePieChart, transactions.stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                                .collect(Collectors.toList()), getResources().getColor(R.color.income));
                        updatePieChart(mExpensePieChart, transactions.stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                                .collect(Collectors.toList()), getResources().getColor(R.color.expense));
                        updatePieChart(mDebtPieChart, transactions.stream()
                                .filter(t -> t.getType() == Transaction.TransactionType.DEBT)
                                .collect(Collectors.toList()), Color.rgb(255, 152, 0));
                        
                        // 更新类别明细
                        updateCategorySummary(transactions);
                        
                        // 更新总览数据
                        updateSummaryData(transactions);
                    }
                });
    }
    
    /**
     * 更新柱状图
     */
    private void updateBarChart(List<Transaction> transactions) {
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        ArrayList<BarEntry> debtEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // 根据日期范围类型生成标签和数据
        Map<Integer, Float> incomeMap = new HashMap<>();
        Map<Integer, Float> expenseMap = new HashMap<>();
        Map<Integer, Float> debtMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        
        int divisionUnit;
        int maxPeriods;
        
        switch (mCurrentDateRange) {
            case DAILY:
                divisionUnit = Calendar.HOUR_OF_DAY;
                maxPeriods = 24;
                break;
            case WEEKLY:
                divisionUnit = Calendar.DAY_OF_WEEK;
                maxPeriods = 7;
                break;
            case MONTHLY:
                divisionUnit = Calendar.DAY_OF_MONTH;
                maxPeriods = 30;
                break;
            case YEARLY:
                divisionUnit = Calendar.MONTH;
                maxPeriods = 12;
                break;
            default:
                divisionUnit = Calendar.DAY_OF_MONTH;
                maxPeriods = 30;
        }
        
        // 初始化数据
        for (int i = 0; i < maxPeriods; i++) {
            incomeMap.put(i, 0f);
            expenseMap.put(i, 0f);
            debtMap.put(i, 0f);
            
            // 根据不同的时间范围生成不同的标签
            if (divisionUnit == Calendar.HOUR_OF_DAY) {
                // 更好的小时显示格式
                if (i < 10) {
                    labels.add(String.format("0%d:00", i));
                } else {
                    labels.add(String.format("%d:00", i));
                }
            } else if (divisionUnit == Calendar.DAY_OF_WEEK) {
                Calendar tempCal = Calendar.getInstance();
                tempCal.add(Calendar.DAY_OF_WEEK, -(7 - i));
                labels.add(String.format("%d/%d", tempCal.get(Calendar.MONTH) + 1, tempCal.get(Calendar.DAY_OF_MONTH)));
            } else if (divisionUnit == Calendar.DAY_OF_MONTH) {
                Calendar tempCal = Calendar.getInstance();
                tempCal.add(Calendar.DAY_OF_MONTH, -(30 - i));
                labels.add(String.format("%d/%d", tempCal.get(Calendar.MONTH) + 1, tempCal.get(Calendar.DAY_OF_MONTH)));
            } else if (divisionUnit == Calendar.MONTH) {
                String[] months = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
                labels.add(months[i]);
            }
        }
        
        // 聚合交易数据
        for (Transaction transaction : transactions) {
            cal.setTime(transaction.getDate());
            int periodIndex;
            
            if (divisionUnit == Calendar.HOUR_OF_DAY) {
                // 对于日维度，直接使用小时作为索引
                periodIndex = cal.get(Calendar.HOUR_OF_DAY);
                
                // 检查是否是当天数据
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                
                Calendar transactionDate = Calendar.getInstance();
                transactionDate.setTime(transaction.getDate());
                transactionDate.set(Calendar.HOUR_OF_DAY, 0);
                transactionDate.set(Calendar.MINUTE, 0);
                transactionDate.set(Calendar.SECOND, 0);
                transactionDate.set(Calendar.MILLISECOND, 0);
                
                // 比较年月日是否相同
                boolean isSameDay = transactionDate.equals(today);
                
                // 如果不是当天数据，跳过
                if (!isSameDay) {
                    continue;
                }
            } else if (divisionUnit == Calendar.DAY_OF_WEEK) {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                
                long diffInMillis = now.getTimeInMillis() - cal.getTimeInMillis();
                int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));
                periodIndex = 6 - Math.min(diffInDays, 6);
            } else if (divisionUnit == Calendar.DAY_OF_MONTH) {
                Calendar now = Calendar.getInstance();
                now.set(Calendar.HOUR_OF_DAY, 0);
                now.set(Calendar.MINUTE, 0);
                now.set(Calendar.SECOND, 0);
                now.set(Calendar.MILLISECOND, 0);
                
                long diffInMillis = now.getTimeInMillis() - cal.getTimeInMillis();
                int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));
                periodIndex = 29 - Math.min(diffInDays, 29);
            } else if (divisionUnit == Calendar.MONTH) {
                periodIndex = cal.get(Calendar.MONTH);
            } else {
                continue;
            }
            
            if (periodIndex >= 0 && periodIndex < maxPeriods) {
                if (transaction.getType() == Transaction.TransactionType.INCOME) {
                    incomeMap.put(periodIndex, incomeMap.get(periodIndex) + (float) transaction.getAmount());
                } else if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                    expenseMap.put(periodIndex, expenseMap.get(periodIndex) + (float) transaction.getAmount());
                } else if (transaction.getType() == Transaction.TransactionType.DEBT) {
                    debtMap.put(periodIndex, debtMap.get(periodIndex) + (float) transaction.getAmount());
                }
            }
        }
        
        // 转换为BarEntry列表
        for (int i = 0; i < maxPeriods; i++) {
            incomeEntries.add(new BarEntry(i, incomeMap.get(i)));
            expenseEntries.add(new BarEntry(i, expenseMap.get(i)));
            debtEntries.add(new BarEntry(i, debtMap.get(i)));
        }
        
        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "收入");
        incomeDataSet.setColor(getResources().getColor(R.color.income));
        
        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "支出");
        expenseDataSet.setColor(getResources().getColor(R.color.expense));
        
        BarDataSet debtDataSet = new BarDataSet(debtEntries, "负债");
        debtDataSet.setColor(Color.BLUE);
        
        float groupSpace = 0.1f;
        float barSpace = 0.02f;
        float barWidth = 0.27f;
        
        // 设置数据到图表
        BarData data = new BarData(incomeDataSet, expenseDataSet, debtDataSet);
        data.setBarWidth(barWidth);
        mBarChart.setData(data);
        mBarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        mBarChart.groupBars(0, groupSpace, barSpace);
        
        // 刷新图表
        mBarChart.invalidate();
    }
    
    /**
     * 更新饼图
     */
    private void updatePieChart(PieChart pieChart, List<Transaction> transactions, int color) {
        // 按类别聚合数据
        Map<String, Float> categoryAmounts = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            float amount = (float) transaction.getAmount();
            
            categoryAmounts.put(category, categoryAmounts.getOrDefault(category, 0f) + amount);
        }
        
        // 转换为PieEntry列表
        ArrayList<PieEntry> entries = new ArrayList<>();
        
        for (Map.Entry<String, Float> entry : categoryAmounts.entrySet()) {
            if (entry.getValue() > 0) {
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }
        
        // 如果没有数据，添加一个"无数据"条目
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "暂无数据"));
        }
        
        // 创建数据集
        PieDataSet dataSet = new PieDataSet(entries, "");
        
        // 设置颜色
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        
        // 设置数据到图表
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new com.github.mikephil.charting.formatter.PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);
        
        pieChart.setData(data);
        
        // 刷新图表
        pieChart.invalidate();
    }
    
    /**
     * 更新类别明细
     */
    private void updateCategorySummary(List<Transaction> transactions) {
        // 按类别聚合数据
        Map<String, Double> incomeCategoryAmounts = new HashMap<>();
        Map<String, Double> expenseCategoryAmounts = new HashMap<>();
        Map<String, Double> debtCategoryAmounts = new HashMap<>();
        double totalIncome = 0;
        double totalExpense = 0;
        double totalDebt = 0;
        
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            double amount = transaction.getAmount();
            
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                incomeCategoryAmounts.put(category, incomeCategoryAmounts.getOrDefault(category, 0.0) + amount);
                totalIncome += amount;
            } else if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                expenseCategoryAmounts.put(category, expenseCategoryAmounts.getOrDefault(category, 0.0) + amount);
                totalExpense += amount;
            } else if (transaction.getType() == Transaction.TransactionType.DEBT) {
                debtCategoryAmounts.put(category, debtCategoryAmounts.getOrDefault(category, 0.0) + amount);
                totalDebt += amount;
            }
        }
        
        // 创建CategorySummary列表
        List<CategorySummary> categorySummaries = new ArrayList<>();
        
        // 添加收入类别
        for (Map.Entry<String, Double> entry : incomeCategoryAmounts.entrySet()) {
            if (totalIncome > 0) {
                float percentage = (float) (entry.getValue() / totalIncome);
                CategorySummary categorySummary = new CategorySummary(
                        entry.getKey(),
                        entry.getValue(),
                        CategorySummary.Type.INCOME
                );
                categorySummary.setPercentage(percentage);
                categorySummaries.add(categorySummary);
            }
        }
        
        // 添加支出类别
        for (Map.Entry<String, Double> entry : expenseCategoryAmounts.entrySet()) {
            if (totalExpense > 0) {
                float percentage = (float) (entry.getValue() / totalExpense);
                CategorySummary categorySummary = new CategorySummary(
                        entry.getKey(),
                        entry.getValue(),
                        CategorySummary.Type.EXPENSE
                );
                categorySummary.setPercentage(percentage);
                categorySummaries.add(categorySummary);
            }
        }
        
        // 更新适配器
        mCategorySummaryAdapter.setData(categorySummaries, true); // 默认显示支出类别
    }
    
    /**
     * 更新总览数据
     */
    private void updateSummaryData(List<Transaction> transactions) {
        double totalIncome = 0;
        double totalExpense = 0;
        double totalDebt = 0;
        
        for (Transaction transaction : transactions) {
            if (transaction.getType() == Transaction.TransactionType.INCOME) {
                totalIncome += transaction.getAmount();
            } else if (transaction.getType() == Transaction.TransactionType.EXPENSE) {
                totalExpense += transaction.getAmount();
            } else if (transaction.getType() == Transaction.TransactionType.DEBT) {
                totalDebt += transaction.getAmount();
            }
        }
        
        double balance = totalIncome - totalExpense;
        
        // 格式化金额
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
        
        // 设置到TextView
        ((TextView) findViewById(R.id.totalIncomeTextView)).setText(currencyFormat.format(totalIncome));
        ((TextView) findViewById(R.id.totalExpenseTextView)).setText(currencyFormat.format(totalExpense));
        ((TextView) findViewById(R.id.balanceTextView)).setText(currencyFormat.format(balance));
        ((TextView) findViewById(R.id.totalDebtTextView)).setText(currencyFormat.format(totalDebt));
        
        // 更新图表
        updateBarChart(transactions);
        updatePieChart(mIncomePieChart, transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .collect(Collectors.toList()), getResources().getColor(R.color.income));
        updatePieChart(mExpensePieChart, transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.toList()), getResources().getColor(R.color.expense));
        updatePieChart(mDebtPieChart, transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.DEBT)
                .collect(Collectors.toList()), Color.rgb(255, 152, 0));
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 