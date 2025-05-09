package com.example.jizhang.ui.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;
import com.example.jizhang.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private final Context mContext;
    private final LayoutInflater mInflater;
    private final OnCategoryClickListener mListener;
    private List<Category> mCategories = new ArrayList<>();
    private String mSelectedCategory;

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mListener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (mCategories != null && position < mCategories.size()) {
            Category category = mCategories.get(position);
            holder.bind(category);
        }
    }

    @Override
    public int getItemCount() {
        return mCategories != null ? mCategories.size() : 0;
    }

    public void setCategories(List<Category> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String categoryName) {
        mSelectedCategory = categoryName;
        notifyDataSetChanged();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final ImageView iconImageView;
        private final TextView nameTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            iconImageView = itemView.findViewById(R.id.categoryIconImageView);
            nameTextView = itemView.findViewById(R.id.categoryNameTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mListener != null) {
                    mListener.onCategoryClick(mCategories.get(position));
                }
            });
        }

        public void bind(Category category) {
            nameTextView.setText(category.getName());
            iconImageView.setImageResource(category.getIconResId());

            boolean isSelected = category.getName().equals(mSelectedCategory);
            int backgroundColor = isSelected 
                    ? ContextCompat.getColor(mContext, R.color.primary_light) 
                    : ContextCompat.getColor(mContext, R.color.white);
            cardView.setCardBackgroundColor(backgroundColor);
        }
    }
} 