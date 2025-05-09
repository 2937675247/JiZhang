package com.example.jizhang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.model.Transaction;
import com.example.jizhang.model.TransactionRepository;
import com.example.jizhang.ui.expense.AddExpenseActivity;
import com.example.jizhang.ui.income.AddIncomeActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {
    private final LayoutInflater mInflater;
    private List<Transaction> mTransactions = new ArrayList<>();
    private final SimpleDateFormat mDateFormat;
    private final NumberFormat mCurrencyFormat;

    public TransactionAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        if (mTransactions != null) {
            Transaction current = mTransactions.get(position);
            holder.descriptionView.setText(current.getDescription());
            holder.dateView.setText(mDateFormat.format(current.getDate()));
            holder.categoryView.setText(current.getCategory());
            
            String amountText = mCurrencyFormat.format(current.getAmount());
            if (current.isExpense()) {
                amountText = "-" + amountText;
                holder.amountView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.expense));
            } else {
                holder.amountView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.income));
            }
            holder.amountView.setText(amountText);
        }
    }

    @Override
    public int getItemCount() {
        return mTransactions == null ? 0 : mTransactions.size();
    }

    public void setTransactions(List<Transaction> transactions) {
        mTransactions = transactions;
        notifyDataSetChanged();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionView;
        private final TextView dateView;
        private final TextView categoryView;
        private final TextView amountView;

        private TransactionViewHolder(View itemView) {
            super(itemView);
            descriptionView = itemView.findViewById(R.id.descriptionTextView);
            dateView = itemView.findViewById(R.id.dateTextView);
            categoryView = itemView.findViewById(R.id.categoryTextView);
            amountView = itemView.findViewById(R.id.amountTextView);
            
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
            String type = transaction.isExpense() ? "支出" : (transaction.isDebt() ? "负债" : "收入");
            typeTextView.setText(type);
            
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.CHINA);
            amountTextView.setText(currencyFormat.format(transaction.getAmount()));
            int colorResId = transaction.isExpense() ? R.color.expense : R.color.income;
            amountTextView.setTextColor(activity.getResources().getColor(colorResId));
            
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
            
            // 添加编辑按钮
            builder.setPositiveButton("编辑", (dialog, which) -> {
                editTransaction(activity, transaction);
            });
            
            // 添加删除按钮
            builder.setNegativeButton("删除", (dialog, which) -> {
                confirmDeleteTransaction(activity, transaction);
            });
            
            // 添加关闭按钮
            builder.setNeutralButton("关闭", null);
            
            builder.show();
        }
        
        /**
         * 编辑交易记录
         */
        private void editTransaction(Activity activity, Transaction transaction) {
            Intent intent;
            
            // 根据交易类型打开相应的编辑界面
            if (transaction.isExpense()) {
                // 打开支出编辑界面
                intent = new Intent(activity, AddExpenseActivity.class);
            } else if (transaction.isDebt()) {
                // 使用专门的负债编辑界面
                intent = new Intent(activity, com.example.jizhang.ui.debt.AddDebtRepaymentActivity.class);
                intent.putExtra("is_debt", true);
                intent.putExtra("debt_type", transaction.getDebtType().toString());
            } else {
                // 打开收入编辑界面
                intent = new Intent(activity, AddIncomeActivity.class);
            }
            
            // 传递交易ID，以便编辑界面加载正确的交易数据
            intent.putExtra("transaction_id", transaction.getId());
            intent.putExtra("is_edit_mode", true);
            
            // 启动编辑活动
            activity.startActivity(intent);
        }
        
        /**
         * 确认删除交易记录
         */
        private void confirmDeleteTransaction(Activity activity, Transaction transaction) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("确认删除");
            builder.setMessage("确定要删除这条交易记录吗？此操作无法撤销。");
            
            builder.setPositiveButton("删除", (dialog, which) -> {
                // 删除交易记录
                TransactionRepository repository = new TransactionRepository(activity.getApplication());
                repository.delete(transaction);
                
                // 发送广播通知数据已更改
                Intent intent = new Intent("com.example.jizhang.DATA_CHANGED");
                activity.sendBroadcast(intent);
                
                // 显示删除成功提示
                Toast.makeText(activity, "交易记录已删除", Toast.LENGTH_SHORT).show();
            });
            
            builder.setNegativeButton("取消", null);
            builder.show();
        }
    }
} 