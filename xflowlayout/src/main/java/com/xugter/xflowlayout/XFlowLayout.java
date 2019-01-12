package com.xugter.xflowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class XFlowLayout extends ViewGroup {

    private ClickListener clickListener;

    private int maxLine = -1;

    private Adapter adapter;

    private final XFlowLayoutDataObserver mObserver;

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
        typedArray.recycle();
        mObserver = new XFlowLayoutDataObserver();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int currentLineMaxHeight = 0;
        int currentXPos = getPaddingLeft();
        int currentYPos = getPaddingTop();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (childWidth + currentXPos <= getWidth() - getPaddingLeft() - getPaddingRight()) {
                child.layout(currentXPos + lp.leftMargin, currentYPos + lp.topMargin, currentXPos + lp.leftMargin + child.getMeasuredWidth(), currentYPos + lp.topMargin + child.getMeasuredHeight());
                currentXPos = currentXPos + childWidth;
                if (childHeight > currentLineMaxHeight) {
                    currentLineMaxHeight = childHeight;
                }
            } else {
                currentYPos = currentYPos + currentLineMaxHeight;
                currentLineMaxHeight = childHeight;
                if (childWidth > getWidth() - getPaddingLeft() - getPaddingRight()) {
                    child.layout(getPaddingLeft() + lp.leftMargin, currentYPos + lp.topMargin, getWidth() - getPaddingRight() - lp.rightMargin, currentYPos + lp.topMargin + child.getMeasuredHeight());
                } else {
                    child.layout(getPaddingLeft() + lp.leftMargin, currentYPos + lp.topMargin, getPaddingLeft() + child.getMeasuredWidth() + lp.leftMargin, currentYPos + lp.topMargin + child.getMeasuredHeight());
                }
                currentXPos = getPaddingLeft() + childWidth;
            }

            if (clickListener != null) {
                final int finalI = i;
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClickOnPos(finalI);
                    }
                });
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
            } else {
                if ((maxLine >= 1 && lineNum >= maxLine) || (heightMode == MeasureSpec.EXACTLY && (getPaddingTop() + getPaddingBottom() + lineNum * childHeight) > heightSize)) {
                    break;
                }
                currentYPos = currentYPos + currentLineMaxHeight;
                currentLineMaxHeight = childHeight;
                currentXPos = getPaddingLeft() + childWidth;
                lineNum++;
            }
            addView(child);
        }
        currentYPos = currentYPos + currentLineMaxHeight;
        setMeasuredDimension(widthSize, (heightMode == MeasureSpec.EXACTLY) ? heightSize : currentYPos + getPaddingBottom());
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setMaxLine(int maxLine) {
        this.maxLine = maxLine;
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.adapter.registerDataObserver(mObserver);

    }

    private class XFlowLayoutDataObserver {
        public void onChanged() {
            XFlowLayout.this.requestLayout();
        }
    }

    public abstract static class Adapter {

        public abstract int getItemCount();

        public abstract View getItemViewByPos(int pos);

        private XFlowLayoutDataObserver observer;

        public void registerDataObserver(XFlowLayoutDataObserver observer) {
            this.observer = observer;
        }

        public void notifyDataChanged() {
            if (observer != null) {
                observer.onChanged();
            }
        }
    }

    public interface ClickListener {
        void onClickOnPos(int pos);
    }
}
