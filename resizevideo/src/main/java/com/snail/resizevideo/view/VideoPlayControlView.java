package com.snail.resizevideo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.snail.resizevideo.R;

import utils.NetworkUtil;


/**
 * Author: snail
 * Data: 18-1-18 上午10:17
 * Des: 视频播放控制器,不同的模式下实现自己的不同的处理吧
 * version:
 */

public class VideoPlayControlView extends BaseVideoPlayControlView
        implements PlayStateSwitcher.OnStateChangeListener,
        PlayModeSwitcher.OnModeChangeListener,
        View.OnClickListener {

    private IVideoView mVideoView;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;
    private PlayStateSwitcher mStateSwitcher;
    private PlayModeSwitcher mModeSwitcher;
    private SeekBar mAutoProgressSeekBar;
    private View mBtnMute;
    private TextView mVideoDuration;
    private int mDurationInMills;
    private boolean mIsMute = true;
    private ImageView mSdvCoverImg;
    private OnOuterActionListener mOnOuterActionListener;
    private Handler mHandler = new Handler();
    private View mPlayControlBar;

    public void setOnOuterActionListener(OnOuterActionListener onOuterActionListener) {
        mOnOuterActionListener = onOuterActionListener;
    }

    public VideoPlayControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public VideoPlayControlView(Context context) {
        this(context, null);
    }

    protected void inflateView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_play_controler, this);
    }

    protected void setUpKeyComponent() {
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setPadding(0, 0, 0, 0);
        mSeekBar.setOnSeekBarChangeListener(this);
        mStateSwitcher = (PlayStateSwitcher) findViewById(R.id.play_switch);
        mStateSwitcher.setOnStateChangeListener(this);
        mBtnMute = findViewById(R.id.btn_mute);
        mBtnMute.setOnClickListener(this);
        mSdvCoverImg = (ImageView) findViewById(R.id.sdv_cover);
        mVideoDuration = (TextView) findViewById(R.id.tv_duration);
        findViewById(R.id.rlv_top_container).setOnClickListener(this);
    }

    protected void setUpAdditionalComponent() {
        mModeSwitcher = (PlayModeSwitcher) findViewById(R.id.state_s);
        mModeSwitcher.setOnModeChangeListener(this);
        mAutoProgressSeekBar = (SeekBar) findViewById(R.id.seek_bar_auto_play);
        mAutoProgressSeekBar.setPadding(0, 0, 0, 0);
        mPlayControlBar = findViewById(R.id.lv_control);
        mPlayControlBar.setVisibility(GONE);
    }

    void init() {
        inflateView();
        setUpKeyComponent();
        setUpAdditionalComponent();
    }

    public void setCoverImgUrl(String url) {

    }

    @Override
    public void bindVideoView(IVideoView videoView) {
        mVideoView = videoView;
    }

    private void showLoadingProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    public void onLoadingStart() {
        showLoadingProgressBar();
    }

    private void onPlayingStart() {
        hideLoadingProgressBar();
        mStateSwitcher.onStartPlay();
        mSdvCoverImg.setVisibility(INVISIBLE);
        if (mOnOuterActionListener != null) {
            mOnOuterActionListener.onPlayStart();
        }
    }

    private void onPreparedFinish() {
        hideLoadingProgressBar();
        mBtnMute.setSelected(false);
        mSeekBar.setEnabled(true);
        mBtnMute.setVisibility(View.VISIBLE);
        mIsMute = true;
        mVideoDuration.setText(getCountDownTimeString(mDurationInMills = mVideoView.getDuration()));
    }

    private void onPlayComplete() {
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        mStateSwitcher.onPlayComplete();
        mVideoDuration.setText(getCountDownTimeString(mDurationInMills = mVideoView.getDuration()));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //  是否有必要跟ios动态更新
//        if (mSeekBar.isPressed()) {
//            mVideoView.seekTo(seekBar.getProgress());
//            mVideoDuration.setText(getCountDownTimeString(mDurationInMills -
//                    (mDurationInMills * seekBar.getProgress() / mSeekBar.getMax())));
//            if (NetworkUtil.isNetworkOpened() && mVideoView.isInPlaybackState()) {
//                showLoadingProgressBar();
//            }
//        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mVideoView.seekTo(seekBar.getProgress());
        mVideoDuration.setText(getCountDownTimeString(mDurationInMills -
                (mDurationInMills * seekBar.getProgress() / mSeekBar.getMax())));
        if (NetworkUtil.isNetworkOpened(getContext()) && mVideoView.isInPlaybackState()) {
            showLoadingProgressBar();
        }
    }


    @Override
    public void onPlayStateChanged(int playState) {

        switch (playState) {
            case AutoSizeVideoView.PlayState.PREPARING:
                onLoadingStart();
                break;
            case AutoSizeVideoView.PlayState.BUFFERING_PLAYING:
                onLoadingStart();
                break;
            case AutoSizeVideoView.PlayState.PLAYING:
                onPlayingStart();
                break;
            case AutoSizeVideoView.PlayState.COMPLETED:
                onPlayComplete();
                break;
            case AutoSizeVideoView.PlayState.PREPARED:
                onPreparedFinish();
                break;
            case AutoSizeVideoView.PlayState.PAUSED:
                onPause();
                break;
        }

    }

    private void onPause() {
        mStateSwitcher.onPause();
        if (mVideoView != null) {
            hideLoadingProgressBar();
            if (mVideoView.getCurrentMode() == AutoSizeVideoView.PlayMode.NORMAL) {
                showControlViews();
            }
        }
    }

    /**
     * 标记完成一次播放2/3处
     */
    private boolean mOneCycle = false;

    @Override
    public void onPlayProgressUpdate(int percent) {
        if (percent != mSeekBar.getProgress() && !mSeekBar.isPressed()) {
            mSeekBar.setProgress(percent);
            mAutoProgressSeekBar.setProgress(percent);
            mVideoDuration.setText(getCountDownTimeString(mDurationInMills - (mDurationInMills * percent / mSeekBar.getMax())));
            if (percent < mSeekBar.getMax() * 0.67) {
                mOneCycle = true;
            } else if (mOneCycle && percent >= mSeekBar.getMax() * 0.67) {
                if (mOnOuterActionListener != null) {
                    mOnOuterActionListener.onCompleteOnce();
                }
                mOneCycle = false;
            }
        }
    }

    /**
     * 被动切换
     */
    @Override
    public void onPlayModeChanged(int playMode) {
        if (playMode != AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
            mHandler.removeCallbacksAndMessages(null);
            showControlViews();
        }
        mModeSwitcher.switchState(playMode);
    }

    /**
     * 主动点击切换
     */
    @Override
    public void onModeChangeClicked(int mode) {
        mVideoView.switchPlayMode(mode);
        if (mode != AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
            if (mVideoView != null && mVideoView.isInPlaybackState()) {
                mHandler.removeCallbacksAndMessages(null);
                showControlViews();
            }
        } else {
            autoHideControls();
            if (mOnOuterActionListener != null) {
                mOnOuterActionListener.onEnterFullScreen();
            }
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {
        mSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
        mAutoProgressSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mStateSwitcher.onPause();
        mSeekBar.setEnabled(false);
        mHandler.removeCallbacksAndMessages(null);
        showControlViews();
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        hideLoadingProgressBar();
    }


    @Override
    public void onPlayClicked() {
        mVideoView.start();
    }

    @Override
    public void onPauseClicked() {
        mVideoView.pause();
        hideLoadingProgressBar();
        if (mOnOuterActionListener != null) {
            mOnOuterActionListener.onPauseClicked();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.removeCallbacksAndMessages(null);
                break;
            case MotionEvent.ACTION_UP:
                autoHideControls();
                break;
        }


        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_mute) {

            if (mIsMute) {
                mVideoView.cancelMute();
                mBtnMute.setSelected(true);
            } else {
                mVideoView.mute();
                mBtnMute.setSelected(false);
            }
            mIsMute = !mIsMute;
        } else if (v.getId() == R.id.rlv_top_container) {
            if (mVideoView != null && mVideoView.isInPlaybackState()) {
                if (mPlayControlBar.getVisibility() == VISIBLE && mVideoView.isPlaying()) {
                    hideControlViews();
                } else {
                    showControlViews();
                }
            }
        }
    }


    private String getCountDownTimeString(int milliseconds) {
        int minute = milliseconds / (1000 * 60);
        int second = (milliseconds / 1000) % 60;
        return String.format("%d:%d", minute, second);
    }

    //在Fragment中使用的时候，会因为detach而暂停
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mVideoView != null && mVideoView.getCurrentState() == AutoSizeVideoView.PlayState.PREPARING) {
            showLoadingProgressBar();
        }
    }


    private void hideControlViews() {
        mPlayControlBar.setVisibility(GONE);
        mAutoProgressSeekBar.setVisibility(VISIBLE);
        if (mOnOuterActionListener != null) {
            mOnOuterActionListener.onHideControlViews(mVideoView.getCurrentMode());
        }
        mBtnMute.setVisibility(mVideoView.getCurrentMode() == AutoSizeVideoView.PlayMode.LANDSCAPE_FULL ?
                View.INVISIBLE :
                View.VISIBLE);
    }

    private void showControlViews() {
        mPlayControlBar.setVisibility(VISIBLE);
        if (mOnOuterActionListener != null) {
            mOnOuterActionListener.onShowControlViews(mVideoView.getCurrentMode());
        }
        mBtnMute.setVisibility(View.VISIBLE);
    }

    /**
     * 延时5s自动隐藏
     */
    private void autoHideControls() {
        if (mVideoView != null && mVideoView.getCurrentMode() == AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mVideoView != null && mVideoView.getCurrentMode() == AutoSizeVideoView.PlayMode.LANDSCAPE_FULL) {
                        hideControlViews();
                    }
                }
            }, 5 * 1000);
        }
    }

    public boolean isControlViewShowing() {
        return mPlayControlBar.getVisibility() == VISIBLE;
    }


}
