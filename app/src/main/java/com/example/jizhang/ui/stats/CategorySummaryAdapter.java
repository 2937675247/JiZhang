package com.example.jizhang.ui.stats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.CategorySummary;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 分类汇总适配器，用于在RecyclerView中显示每个类别的消费/收入情况
 */
public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.ViewHolder> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    
    private List<CategorySummary> categorySummaries = new ArrayList<>();
    private boolean isExpense = true; // 默认显示支出
    private Context context;

    public CategorySummaryAdapter(Context context) {
        this.context = context;
    }

    /**
     * 设置数据并刷新视图
     * @param categorySummaries 分类汇总数据
     * @param isExpense 是否为支出分类
     */
    public void setData(List<CategorySummary> categorySummaries, boolean isExpense) {
        this.categorySummaries = categorySummaries;
        this.isExpense = isExpense;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_summary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategorySummary summary = categorySummaries.get(position);
        
        holder.categoryNameTextView.setText(summary.getCategoryName());
        holder.amountTextView.setText("¥" + DECIMAL_FORMAT.format(summary.getAmount()));
        
        // 设置进度条百分比
        holder.percentageBar.setProgress((int) (summary.getPercentage() * 100));
        
        // 设置颜色：支出为红色，收入为绿色
        int color;
        if (summary.getType() != null) {
            // 如果有指定类型，根据类型设置颜色
            color = summary.getType() == CategorySummary.Type.EXPENSE ? 
                    Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
        } else {
            // 否则根据当前显示模式设置颜色
            color = isExpense ? Color.parseColor("#F44336") : Color.parseColor("#4CAF50");
        }
        
        holder.amountTextView.setTextColor(color);
        holder.percentageBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return categorySummaries.size();
    }

    /**
     * 视图持有者
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView amountTextView;
        ProgressBar percentageBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.tv_category_name);
            amountTextView = itemView.findViewById(R.id.tv_amount);
            percentageBar = itemView.findViewById(R.id.progress_bar_percentage);
        }
    }
} 