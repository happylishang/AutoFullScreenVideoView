package com.snail.resizevideo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.snail.resizevideo.R;

import utils.NetworkUtil;


/**
 * Author: snail
 * Data: 18-1-19 上午10:28
 * Des:
 * version:
 */

public class PlayStateSwitcher extends FrameLayout implements View.OnClickListener {


    private final static int STATE_PLAYING = 1;//播放状态
    private final static int STATE_PAUSE = 2;//暂停状态
    private int mCurrentState = STATE_PAUSE;
    private TextView mBtnSwitcher;

    public void setOnStateChangeListener(OnStateChangeListener onStateChangeListener) {
        mOnStateChangeListener = onStateChangeListener;
    }

    private OnStateChangeListener mOnStateChangeListener;

    @Override
    public void onClick(View v) {
        if (mOnStateChangeListener != null) {
            if (mCurrentState != STATE_PAUSE) {
                mOnStateChangeListener.onPauseClicked();
                mCurrentState = STATE_PAUSE;
                switchState(mCurrentState);
            } else {
                if (!NetworkUtil.isNetworkOpened(getContext())) {
                    Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
                    return;
                }
                mOnStateChangeListener.onPlayClicked();
                mCurrentState = STATE_PLAYING;
                switchState(mCurrentState);
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
        mBtnSwitcher = new TextView(getContext());
        this.addView(mBtnSwitcher, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setOnClickListener(this);
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
            mBtnSwitcher.setBackground(getResources().getDrawable(R.drawable.goods_video_ic_pause));
        } else {
            mBtnSwitcher.setBackground(getResources().getDrawable(R.drawable.goods_video_ic_play));
        }
    }
}
