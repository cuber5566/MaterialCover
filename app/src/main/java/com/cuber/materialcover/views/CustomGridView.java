package com.cuber.materialcover.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.GridLayoutAnimationController;

import com.cuber.materialcover.R;

public class CustomGridView extends RecyclerView {

    private int defaultCellWidth;

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttrs(context, attrs);
    }

    private void setAttrs(Context context, AttributeSet attrs) {
        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray typedArray = theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomGridView,
                    0, 0);
            if (typedArray != null) {
                try {
                    defaultCellWidth = (int) typedArray.getDimension(R.styleable.CustomGridView_defaultItemWidth, 0);
                } finally {
                    typedArray.recycle();
                }
            }
        }
    }

    @Override
    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {

        if (getAdapter() != null && getLayoutManager() instanceof GridLayoutManager){

            GridLayoutAnimationController.AnimationParameters animationParams =
                    (GridLayoutAnimationController.AnimationParameters) params.layoutAnimationParameters;

            if (animationParams == null) {
                animationParams = new GridLayoutAnimationController.AnimationParameters();
                params.layoutAnimationParameters = animationParams;
            }

            //抓有幾個行
            int columns = ((GridLayoutManager) getLayoutManager()).getSpanCount();

            animationParams.count = count;
            animationParams.index = index;
            animationParams.columnsCount = columns;
            animationParams.rowsCount = count / columns;

            final int invertedIndex = count - 1 - index;
            animationParams.column = columns - 1 - (invertedIndex % columns);
            animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns;
            Log.i("AnimationParameters", "count:"+count+", index:"+index+", columnsCount:"+columns+", rowsCount:"+animationParams.rowsCount+", column:" +animationParams.column+", row:"+animationParams.row);
        } else {
            super.attachLayoutAnimationParameters(child, params, index, count);
        }
    }



    public int getDefaultCellWidth(){
        return defaultCellWidth;
    }

    public void setDefaultCellWidth(int width){
        this.defaultCellWidth = width;
    }

}
