package com.xugter.xflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class XFlowLayout extends ViewGroup {
    private int maxLine = -1;

    private Adapter adapter;

    private final XFlowLayoutDataObserver mObserver;

    private SparseIntArray lineWidthInfo = new SparseIntArray();

    private boolean centerHorizontal = false;

    public XFlowLayout(Context context) {
        this(context, null);
    }

    public XFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XFlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.XFlowLayout, defStyleAttr, 0);
        maxLine = typedArray.getInteger(R.styleable.XFlowLayout_max_line, -1);
        centerHorizontal = typedArray.getBoolean(R.styleable.XFlowLayout_center_horizontal, false);
        typedArray.recycle();
        mObserver = new XFlowLayoutDataObserver();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int currentLineMaxHeight = 0;
        int currentXPos = getPaddingLeft();
        int currentYPos = getPaddingTop();

        int currentLine = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (childWidth + currentXPos <= getWidth() - getPaddingLeft() - getPaddingRight()) {
                if (currentLine == 0 && centerHorizontal) {
                    currentLine++;
                    currentXPos = (getWidth() - getPaddingLeft() - getPaddingRight() - lineWidthInfo.get(currentLine)) / 2 + getPaddingLeft();
                }
                child.layout(currentXPos + lp.leftMargin, currentYPos + lp.topMargin, currentXPos + lp.leftMargin + child.getMeasuredWidth(), currentYPos + lp.topMargin + child.getMeasuredHeight());
                currentXPos = currentXPos + childWidth;
                if (childHeight > currentLineMaxHeight) {
                    currentLineMaxHeight = childHeight;
                }
            } else {
                currentYPos = currentYPos + currentLineMaxHeight;
                currentLineMaxHeight = childHeight;
                currentLine++;
                if (centerHorizontal) {
                    currentXPos = (getWidth() - getPaddingLeft() - getPaddingRight() - lineWidthInfo.get(currentLine)) / 2 + getPaddingLeft();
                } else {
                    currentXPos = getPaddingLeft();
                }
                if (childWidth > getWidth() - getPaddingLeft() - getPaddingRight()) {
                    child.layout(getPaddingLeft() + lp.leftMargin, currentYPos + lp.topMargin, getWidth() - getPaddingRight() - lp.rightMargin, currentYPos + lp.topMargin + child.getMeasuredHeight());
                } else {
                    child.layout(currentXPos + lp.leftMargin, currentYPos + lp.topMargin, currentXPos + child.getMeasuredWidth() + lp.leftMargin, currentYPos + lp.topMargin + child.getMeasuredHeight());
                }
                currentXPos = currentXPos + childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (adapter == null) {
            setMeasuredDimension(0, 0);
            return;
        }

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int currentXPos = getPaddingLeft();
        int currentYPos = getPaddingTop();

        int lineNum = 0;
        int currentLineMaxHeight = 0;

        removeAllViews();

        int currentWidth = 0;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            View child = adapter.getItemViewByPos(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (lineNum == 0) {
                lineNum = 1;
            }
            if (childWidth + currentXPos <= widthSize - getPaddingLeft() - getPaddingRight()) {
                currentXPos = currentXPos + childWidth;
                if (childHeight > currentLineMaxHeight) {
                    currentLineMaxHeight = childHeight;
                }
                currentWidth += childWidth;
            } else {
                if ((maxLine >= 1 && lineNum >= maxLine) || (heightMode == MeasureSpec.EXACTLY && (getPaddingTop() + getPaddingBottom() + lineNum * childHeight) > heightSize)) {
                    break;
                }
                currentYPos = currentYPos + currentLineMaxHeight;
                currentLineMaxHeight = childHeight;
                currentXPos = getPaddingLeft() + childWidth;
                if (centerHorizontal) {
                    lineWidthInfo.append(lineNum, currentWidth);
                }
                currentWidth = childWidth;
                lineNum++;
            }
            addView(child);
        }
        currentYPos = currentYPos + currentLineMaxHeight;
        if (centerHorizontal) {
            lineWidthInfo.append(lineNum, currentWidth);
        }
        setMeasuredDimension(widthSize, (heightMode == MeasureSpec.EXACTLY) ? heightSize : currentYPos + getPaddingBottom());
    }

    /**
     * setup max line to show
     *
     * @param maxLine must bigger than 0
     */
    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    /**
     * setup center horizontal
     *
     * @param enable true center horizontal, false left to right
     */
    public void setCenterHorizontal(boolean enable) {
        centerHorizontal = enable;
    }

    /**
     * set adapter for xflowlayout to display
     *
     * @param adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.adapter.registerDataObserver(mObserver);
    }

    private class XFlowLayoutDataObserver {
        void onChanged() {
            XFlowLayout.this.requestLayout();
        }
    }

    public abstract static class Adapter {

        public abstract int getItemCount();

        public abstract View getItemViewByPos(int pos);

        private List<XFlowLayoutDataObserver> observers = new ArrayList<>();

        void registerDataObserver(XFlowLayoutDataObserver observer) {
            observers.add(observer);
        }

        public void notifyDataChanged() {
            for (XFlowLayoutDataObserver observer : observers) {
                observer.onChanged();
            }
        }
    }
}
