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

public class PlayStateSwitcher extends FrameLayout implements View.OnClickListener {


    private final static int STATE_PLAYING = 1;
    private final static int STATE_PAUSE = 2;
    private int mCurrentState = STATE_PAUSE;
    private Button mBtnSwitcher;

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    private OnStateChangeListener mOnStateChangeListener;

    @Override
    public void onClick(View v) {
        mCurrentState = mCurrentState != STATE_PAUSE ? STATE_PAUSE : STATE_PLAYING;
        switchState(mCurrentState);
        if (mOnStateChangeListener != null) {
            if (mCurrentState == STATE_PAUSE) {
                mOnStateChangeListener.onPauseClicked();
            } else {
                mOnStateChangeListener.onPlayClicked();
            }
        }
    }

    public interface OnStateChangeListener {

        void onPlayClicked();

        void onPauseClicked();
    }

    public PlayStateSwitcher(Context context) {
        this(context, null);
    }

    public PlayStateSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayStateSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBtnSwitcher = new Button(getContext());
        mBtnSwitcher.setText("播放");
        this.addView(mBtnSwitcher, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mBtnSwitcher.setOnClickListener(this);
    }

    public void onPlayComplete() {
        switchState(STATE_PAUSE);
    }

    public void onStartPlay() {
        switchState(STATE_PLAYING);
    }

    public void onPause() {
        switchState(STATE_PAUSE);
    }

    private void switchState(int state) {
        mCurrentState = state;
        if (state == STATE_PLAYING) {
            mBtnSwitcher.setText("暂停");
        } else {
            mBtnSwitcher.setText("播放");
        }
    }
}
