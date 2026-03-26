package com.example.smd_assigment_1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * A ListView that scrolls horizontally by swapping width/height in measurement/layout.
 * (Required to keep the "Deals of the Day" section as a ListView.)
 */
public class HorizontalListView extends ListView {

    public HorizontalListView(Context context) {
        super(context);
    }

    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, top, left, bottom, right);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(y, x);
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollRange() {
        return super.computeVerticalScrollRange();
    }
}

