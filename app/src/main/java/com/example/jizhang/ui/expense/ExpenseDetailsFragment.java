package com.example.jizhang.ui.expense;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseDetailsFragment extends Fragment implements SwipeToDeleteCallback.SwipeToDeleteListener {

    private ExpenseDetailsViewModel mViewModel;
    private TextView mTotalExpenseTextView;
    private RecyclerView mExpenseRecyclerView;
    private TextView mEmptyView;
    private TransactionAdapter mAdapter;
    private PieChart mPieChart;
    private FrameLayout mChartContainer;
    private List<Transaction> mTransactionList = new ArrayList<>();

    public static ExpenseDetailsFragment newInstance() {
        return new ExpenseDetailsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_expense_details, container, false);

        // 初始化视图
        mTotalExpenseTextView = root.findViewById(R.id.totalExpenseTextView);
        mExpenseRecyclerView = root.findViewById(R.id.expenseRecyclerView);
        mEmptyView = root.findViewById(R.id.emptyView);
        mChartContainer = root.findViewById(R.id.chartContainer);

        // 设置饼图
        setupPieChart();

        // 设置RecyclerView
        mAdapter = new TransactionAdapter(mTransactionList);
        mExpenseRecyclerView.setAdapter(mAdapter);
        mExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mExpenseRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        
        // 添加左滑删除功能
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext(), this);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(mExpenseRecyclerView);

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
        mPieChart.setCenterText("支出分类"); // 设置中心文本
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
        mViewModel = new ViewModelProvider(this).get(ExpenseDetailsViewModel.class);

        // 观察总支出数据
        mViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            if (expense != null) {
                updateTotalExpenseView(expense);
            } else {
                updateTotalExpenseView(0);
            }
        });

        // 观察支出交易列表
        mViewModel.getExpenseTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                mTransactionList.clear();
                mTransactionList.addAll(transactions);
                mAdapter.notifyDataSetChanged();
                
                mExpenseRecyclerView.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.GONE);
                
                // 更新饼图数据
                updateChart(mTransactionList);
            } else {
                mTransactionList.clear();
                mAdapter.notifyDataSetChanged();
                
                mExpenseRecyclerView.setVisibility(View.GONE);
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
        // 按类别聚合数据
        Map<String, Float> categoryAmounts = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            String category = transaction.getCategory();
            float amount = (float) transaction.getAmount();
            
            categoryAmounts.put(category, categoryAmounts.getOrDefault(category, 0f) + amount);
        }
        
        // 创建饼图数据
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
        
        // 设置数据集
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f); // 设置饼块间距
        dataSet.setSelectionShift(5f); // 设置选中饼块的偏移量
        
        // 设置颜色
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) {
            colors.add(c);
        }
        for (int c : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        
        // 创建饼图数据对象
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(mPieChart)); // 设置百分比格式化器
        data.setValueTextSize(12f); // 设置值文本大小
        data.setValueTextColor(Color.WHITE); // 设置值文本颜色
        
        // 设置数据
        mPieChart.setData(data);
        
        // 刷新
        mPieChart.invalidate();
        mPieChart.animateY(1000); // 添加动画
    }

    /**
     * 更新总支出显示
     */
    private void updateTotalExpenseView(double expense) {
        String formattedExpense = NumberFormat.getCurrencyInstance(Locale.CHINA).format(expense);
        mTotalExpenseTextView.setText(formattedExpense);
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
    private class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

        private List<Transaction> mTransactions;
        private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        public TransactionAdapter(List<Transaction> transactions) {
            mTransactions = transactions;
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            Transaction transaction = mTransactions.get(position);
            holder.bind(transaction);
        }

        @Override
        public int getItemCount() {
            return mTransactions != null ? mTransactions.size() : 0;
        }

        /**
         * 交易ViewHolder
         */
        class TransactionViewHolder extends RecyclerView.ViewHolder {
            private final TextView categoryTextView;
            private final TextView amountTextView;
            private final TextView dateTextView;
            private final TextView descriptionTextView;

            public TransactionViewHolder(@NonNull View itemView) {
                super(itemView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                amountTextView = itemView.findViewById(R.id.amountTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
                
                // 设置点击事件
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemView.getContext() instanceof Activity) {
                        Activity activity = (Activity) itemView.getContext();
                        Transaction transaction = mTransactions.get(position);
                        showTransactionDetails(activity, transaction);
                    }
                });
            }

            public void bind(Transaction transaction) {
                categoryTextView.setText(transaction.getCategory());
                
                // 格式化金额
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
                amountTextView.setText(currencyFormat.format(transaction.getAmount()));
                amountTextView.setTextColor(Color.parseColor("#F44336")); // 红色表示支出
                
                // 格式化日期
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dateTextView.setText(dateFormat.format(transaction.getDate()));
                
                // 设置描述
                descriptionTextView.setText(transaction.getDescription());
            }
            
            /**
             * 显示交易详情对话框
             */
            private void showTransactionDetails(Activity activity, Transaction transaction) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("交易详情");
                
                // 创建对话框内容视图
                View view = LayoutInflater.from(activity).inflate(R.layout.dialog_transaction_details, null);
                
                // 初始化视图控件
                TextView typeTextView = view.findViewById(R.id.detailTypeTextView);
                TextView amountTextView = view.findViewById(R.id.detailAmountTextView);
                TextView categoryTextView = view.findViewById(R.id.detailCategoryTextView);
                TextView dateTextView = view.findViewById(R.id.detailDateTextView);
                TextView descriptionTextView = view.findViewById(R.id.detailDescriptionTextView);
                TextView transactionNoTextView = view.findViewById(R.id.detailTransactionNoTextView);
                
                // 设置数据
                typeTextView.setText("支出");
                
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
                amountTextView.setText(currencyFormat.format(transaction.getAmount()));
                amountTextView.setTextColor(Color.parseColor("#F44336")); // 红色表示支出
                
                categoryTextView.setText(transaction.getCategory());
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dateTextView.setText(dateFormat.format(transaction.getDate()));
                
                descriptionTextView.setText(transaction.getDescription());
                
                String transactionNo = transaction.getPaymentTransactionNo();
                if (transactionNo != null && !transactionNo.isEmpty()) {
                    transactionNoTextView.setText(transactionNo);
                } else {
                    transactionNoTextView.setText("无");
                }
                
                builder.setView(view);
                builder.setPositiveButton("关闭", null);
                builder.show();
            }
        }
    }
} 