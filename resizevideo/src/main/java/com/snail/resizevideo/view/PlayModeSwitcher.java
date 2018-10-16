package com.snail.resizevideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.snail.resizevideo.R;

/**
 * Author: snail
 * Data: 18-1-19 上午10:28
 * Des:
 * version:
 */

public class PlayModeSwitcher extends FrameLayout implements View.OnClickListener {


    private int mCurrentState = AutoSizeVideoView.PlayMode.NORMAL;
    private TextView mBtnSwitcher;
    private OnModeChangeListener mOnModeChangeListener;

    public interface OnModeChangeListener {
        void onModeChangeClicked(int mode);
    }

    public void setOnModeChangeListener(OnModeChangeListener onModeChangeListener) {
        mOnModeChangeListener = onModeChangeListener;
    }


    @Override
    public void onClick(View v) {
        mCurrentState = mCurrentState != AutoSizeVideoView.PlayMode.NORMAL ?
                AutoSizeVideoView.PlayMode.NORMAL :
                AutoSizeVideoView.PlayMode.LANDSCAPE_FULL;
        switchState(mCurrentState);
        if (mOnModeChangeListener != null) {
            mOnModeChangeListener.onModeChangeClicked(mCurrentState);
        }
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
        mBtnSwitcher = new TextView(getContext());
        mBtnSwitcher.setBackground(getResources().getDrawable(R.drawable.goods_video_ic_fullscreen));
        this.addView(mBtnSwitcher, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setOnClickListener(this);
    }


    public void switchState(int state) {
        mCurrentState = state;
        if (state == AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
            mBtnSwitcher.setBackground(getResources().getDrawable(R.drawable.goods_video_ic_smallscreen));
        } else {
            mBtnSwitcher.setBackground(getResources().getDrawable(R.drawable.goods_video_ic_fullscreen));
        }
    }
}
