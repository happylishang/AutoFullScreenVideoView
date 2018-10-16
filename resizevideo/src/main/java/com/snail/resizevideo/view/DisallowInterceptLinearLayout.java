package com.snail.resizevideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Author: snail
 * Data: 18-1-25 下午8:55
 * Des:
 * version:
 */

public class DisallowInterceptLinearLayout extends LinearLayout {

    public DisallowInterceptLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DisallowInterceptLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DisallowInterceptLinearLayout(Context context) {
        this(context, null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }
}
