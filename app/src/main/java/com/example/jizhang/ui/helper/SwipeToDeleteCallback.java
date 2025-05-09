package com.example.jizhang.ui.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jizhang.R;

/**
 * 通用的左滑删除回调
 * 继承ItemTouchHelper.Callback实现左滑显示删除按钮的功能
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {
    
    private final SwipeToDeleteListener mListener;
    private final ColorDrawable mBackground;
    private final int mDeleteIconMargin;
    private Drawable mDeleteIcon;
    private Paint mTextPaint;
    
    public interface SwipeToDeleteListener {
        /**
         * 当用户左滑并释放时，触发删除操作
         *
         * @param position 要删除的项目位置
         */
        void onItemDelete(int position);
    }
    
    public SwipeToDeleteCallback(Context context, SwipeToDeleteListener listener) {
        this.mListener = listener;
        this.mBackground = new ColorDrawable(Color.parseColor("#F44336"));
        this.mDeleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
        this.mDeleteIconMargin = 16;
        
        // 初始化删除按钮的文本画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(48);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 只支持左滑，不支持拖拽
        int swipeFlags = ItemTouchHelper.LEFT;
        return makeMovementFlags(0, swipeFlags);
    }
    
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // 不支持拖拽，返回false
        return false;
    }
    
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // 当滑动完成时，通知监听器执行删除操作
        int position = viewHolder.getAdapterPosition();
        mListener.onItemDelete(position);
    }
    
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        
        // 如果不是正在滑动，跳过绘制
        if (dX == 0) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        
        // 设置背景
        mBackground.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        mBackground.draw(c);
        
        // 计算删除图标的位置
        if (mDeleteIcon != null) {
            int iconHeight = mDeleteIcon.getIntrinsicHeight();
            int iconWidth = mDeleteIcon.getIntrinsicWidth();
            int iconTop = itemView.getTop() + (itemView.getHeight() - iconHeight) / 2;
            int iconLeft = itemView.getRight() - iconWidth - mDeleteIconMargin * 2;
            
            // 设置删除图标的位置
            mDeleteIcon.setBounds(
                    iconLeft,
                    iconTop,
                    iconLeft + iconWidth,
                    iconTop + iconHeight
            );
            
            // 当滑动距离足够时才绘制图标
            if (Math.abs(dX) > iconWidth + mDeleteIconMargin * 3) {
                mDeleteIcon.draw(c);
                
                // 绘制"删除"文字
                String deleteText = "删除";
                Rect textBounds = new Rect();
                mTextPaint.getTextBounds(deleteText, 0, deleteText.length(), textBounds);
                
                float textX = itemView.getRight() - iconWidth - mDeleteIconMargin * 5 - textBounds.width();
                float textY = itemView.getTop() + (itemView.getHeight() + textBounds.height()) / 2f;
                
                c.drawText(deleteText, textX, textY, mTextPaint);
            }
        }
        
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
    
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        // 设置滑动阈值，当滑动超过这个比例时触发删除
        return 0.5f;
    }
} 