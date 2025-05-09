package com.example.jizhang.ui.debt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.Transaction;
import com.example.jizhang.ui.helper.SwipeToDeleteCallback;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DebtDetailsFragment extends Fragment implements SwipeToDeleteCallback.SwipeToDeleteListener {

    private DebtDetailsViewModel mViewModel;
    private TextView mTotalDebtTextView;
    private RecyclerView mDebtRecyclerView;
    private TextView mEmptyView;
    private TransactionAdapter mAdapter;
    private PieChart mPieChart;
    private FrameLayout mChartContainer;
    private List<Transaction> mTransactionList = new ArrayList<>();

    public static DebtDetailsFragment newInstance() {
        return new DebtDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_debt_details, container, false);

        // 初始化视图
        mTotalDebtTextView = root.findViewById(R.id.totalDebtTextView);
        mDebtRecyclerView = root.findViewById(R.id.debtRecyclerView);
        mEmptyView = root.findViewById(R.id.emptyView);
        mChartContainer = root.findViewById(R.id.chartContainer);

        // 设置饼图
        setupPieChart();

        // 设置RecyclerView
        mAdapter = new TransactionAdapter();
        mDebtRecyclerView.setAdapter(mAdapter);
        mDebtRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mDebtRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        
        // 添加左滑删除功能
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext(), this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(mDebtRecyclerView);

        return root;
    }

    /**
     * 设置饼图
     */
    private void setupPieChart() {
        // 清空容器
        mChartContainer.removeAllViews();

        // 创建饼图
        mPieChart = new PieChart(requireContext());
        mChartContainer.addView(mPieChart);

        // 配置饼图
        mPieChart.getDescription().setEnabled(false); // 隐藏描述
        mPieChart.setUsePercentValues(true); // 使用百分比值
        mPieChart.setDrawHoleEnabled(true); // 绘制中心空洞
        mPieChart.setHoleColor(Color.WHITE); // 设置空洞颜色
        mPieChart.setHoleRadius(50f); // 设置空洞半径
        mPieChart.setTransparentCircleRadius(55f); // 设置透明圆半径
        mPieChart.setDrawCenterText(true); // 绘制中心文本
        mPieChart.setCenterText("负债分类"); // 设置中心文本
        mPieChart.setCenterTextSize(14f); // 设置中心文本大小
        mPieChart.setRotationAngle(0); // 设置旋转角度
        mPieChart.setRotationEnabled(true); // 允许旋转
        mPieChart.setHighlightPerTapEnabled(true); // 允许点击高亮
        
        // 设置图例
        Legend legend = mPieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM); // 垂直对齐方式
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER); // 水平对齐方式
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL); // 方向
        legend.setDrawInside(false); // 不在内部绘制
        legend.setXEntrySpace(5f); // X轴条目间距
        legend.setYEntrySpace(0f); // Y轴条目间距
        legend.setWordWrapEnabled(true); // 自动换行
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化ViewModel
        mViewModel = new ViewModelProvider(this).get(DebtDetailsViewModel.class);

        // 观察总负债数据
        mViewModel.getTotalDebt().observe(getViewLifecycleOwner(), totalDebt -> {
            if (totalDebt != null) {
                updateTotalDebtView(totalDebt);
            } else {
                updateTotalDebtView(0);
            }
        });

        // 观察负债交易列表
        mViewModel.getDebtTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                mTransactionList.clear();
                mTransactionList.addAll(transactions);
                mAdapter.setTransactions(transactions);
                
                mDebtRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                
                // 更新饼图数据
                updateChart(transactions);
            } else {
                mTransactionList.clear();
                mAdapter.setTransactions(new ArrayList<>());
                
                mDebtRecyclerView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                
                // 清空饼图
                updateChart(new ArrayList<>());
            }
        });
    }

    /**
     * 更新饼图数据
     */
    private void updateChart(List<Transaction> transactions) {
        Map<String, Float> categoryTotals = new HashMap<>();
        float repaymentTotal = 0f;
        
        for (Transaction transaction : transactions) {
            float amount = (float)transaction.getAmount();
            if (amount >= 0) {
                // 正金额为负债
                String category = transaction.getCategory();
                float currentTotal = categoryTotals.getOrDefault(category, 0f);
                categoryTotals.put(category, currentTotal + amount);
            } else {
                // 负金额为还款，取绝对值累加
                repaymentTotal += Math.abs(amount);
            }
        }
        
        List<PieEntry> entries = new ArrayList<>();
        
        // 添加正金额的负债类别
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            if (entry.getValue() > 0) {  // 确保只添加正值
                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            }
        }
        
        // 添加还款类别（如果有还款记录）
        if (repaymentTotal > 0) {
            entries.add(new PieEntry(repaymentTotal, "还款"));
        }
        
        // 如果没有数据，添加一个空的条目
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f, "暂无数据"));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "负债分布");
        
        // 使用预定义颜色并为还款设置特殊颜色
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        
        // 如果有还款记录，为其设置绿色
        if (repaymentTotal > 0) {
            colors.set(colors.size() - 1, Color.rgb(76, 175, 80)); // 绿色
        }
        
        dataSet.setColors(colors);
        
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueFormatter(new PercentFormatter(mPieChart));
        
        mPieChart.setData(data);
        mPieChart.getDescription().setEnabled(false);
        mPieChart.setCenterText("负债\n分布");
        mPieChart.setCenterTextSize(14f);
        mPieChart.invalidate();
    }

    /**
     * 更新总负债显示
     */
    private void updateTotalDebtView(double totalDebt) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("CNY"));
        mTotalDebtTextView.setText(format.format(totalDebt));
    }

    // 实现SwipeToDeleteCallback.SwipeToDeleteListener接口
    @Override
    public void onItemDelete(int position) {
        if (position >= 0 && position < mTransactionList.size()) {
            Transaction deleteTransaction = mTransactionList.get(position);
            
            // 从数据库中删除
            mViewModel.deleteTransaction(deleteTransaction);
            
            // 显示提示
            Toast.makeText(requireContext(), "已删除记录", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 交易列表适配器
     */
    public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
        private List<Transaction> mTransactions = new ArrayList<>();
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private NumberFormat mCurrencyFormat;

        public TransactionAdapter() {
            mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            mCurrencyFormat.setCurrency(Currency.getInstance("CNY"));
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            if (position < mTransactions.size()) {
                holder.bind(mTransactions.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mTransactions.size();
        }

        public void setTransactions(List<Transaction> transactions) {
            mTransactions = transactions;
            notifyDataSetChanged();
        }

        public class TransactionViewHolder extends RecyclerView.ViewHolder {
            private TextView mDescriptionTextView;
            private TextView mDateTextView;
            private TextView mCategoryTextView;
            private TextView mAmountTextView;

            public TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                mDescriptionTextView = itemView.findViewById(R.id.descriptionTextView);
                mDateTextView = itemView.findViewById(R.id.dateTextView);
                mCategoryTextView = itemView.findViewById(R.id.categoryTextView);
                mAmountTextView = itemView.findViewById(R.id.amountTextView);
                
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < mTransactions.size()) {
                        Transaction transaction = mTransactions.get(position);
                        if (getContext() instanceof Activity) {
                            showTransactionDetails(transaction, (Activity) getContext());
                        }
                    }
                });
            }

            public void bind(Transaction transaction) {
                mDescriptionTextView.setText(transaction.getDescription());
                mDateTextView.setText(mDateFormat.format(transaction.getDate()));
                mCategoryTextView.setText(transaction.getCategory());
                mAmountTextView.setText(mCurrencyFormat.format(transaction.getAmount()));
                
                // 设置债务金额颜色为红色
                mAmountTextView.setTextColor(getResources().getColor(R.color.colorExpense));
            }
            
            private void showTransactionDetails(Transaction transaction, Activity activity) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_transaction_details, null);
                
                TextView typeTextView = dialogView.findViewById(R.id.detailTypeTextView);
                TextView amountTextView = dialogView.findViewById(R.id.detailAmountTextView);
                TextView categoryTextView = dialogView.findViewById(R.id.detailCategoryTextView);
                TextView dateTextView = dialogView.findViewById(R.id.detailDateTextView);
                TextView descriptionTextView = dialogView.findViewById(R.id.detailDescriptionTextView);
                TextView transactionNoTextView = dialogView.findViewById(R.id.detailTransactionNoTextView);
                
                typeTextView.setText("负债");
                typeTextView.setTextColor(getResources().getColor(R.color.colorExpense));
                
                amountTextView.setText(mCurrencyFormat.format(transaction.getAmount()));
                amountTextView.setTextColor(getResources().getColor(R.color.colorExpense));
                
                categoryTextView.setText(transaction.getCategory());
                dateTextView.setText(mDateFormat.format(transaction.getDate()));
                descriptionTextView.setText(transaction.getDescription());
                
                String transactionNo = transaction.getPaymentTransactionNo();
                if (transactionNo != null && !transactionNo.isEmpty()) {
                    transactionNoTextView.setText(transactionNo);
                } else {
                    transactionNoTextView.setText("无");
                }
                
                builder.setView(dialogView)
                        .setTitle("交易详情")
                        .setPositiveButton("关闭", null)
                        .show();
            }
        }
    }
} 