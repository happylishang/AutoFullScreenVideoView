package com.snail.resizevideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Author: snail
 * Data: 18-1-19 上午10:28
 * Des:
 * version:
 */

public class PlayModeSwitcher extends FrameLayout implements View.OnClickListener {


    private int mCurrentState = AutoSizeVideoView.PlayMode.NORMAL;
    private Button mBtnSwitcher;

    public void setOnModeChangeListener(OnModeChangeListener onModeChangeListener) {
        mOnModeChangeListener = onModeChangeListener;
    }

    private OnModeChangeListener mOnModeChangeListener;

    @Override
    public void onClick(View v) {
        mCurrentState = mCurrentState != AutoSizeVideoView.PlayMode.NORMAL ?
                AutoSizeVideoView.PlayMode.NORMAL :
                AutoSizeVideoView.PlayMode.LANDSCAPE_FULL;
        switchState(mCurrentState);
        if (mOnModeChangeListener != null) {
            mOnModeChangeListener.onModeChange(mCurrentState);
        }
    }

    public interface OnModeChangeListener {
        void onModeChange(int mode);
    }

    public PlayModeSwitcher(Context context) {
        this(context, null);
    }

    public PlayModeSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayModeSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBtnSwitcher = new Button(getContext());
        mBtnSwitcher.setText("切换全屏");
        this.addView(mBtnSwitcher, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mBtnSwitcher.setOnClickListener(this);
    }


    private void switchState(int state) {
        mCurrentState = state;
        if (state == AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
            mBtnSwitcher.setText("切换普通");
        } else {
            mBtnSwitcher.setText("切换全屏");
        }
    }
}
