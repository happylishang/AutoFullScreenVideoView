package com.snail.resizevideo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.snail.resizevideo.R;


/**
 * Author: snail
 * Data: 18-1-18 上午10:17
 * Des: 视频播放控制器,不同的模式下实现自己的不同的处理吧
 * version:
 */

public class VideoPlayControlView extends FrameLayout
        implements SeekBar.OnSeekBarChangeListener,
        IPlayStateListener,
        PlayStateSwitcher.OnStateChangeListener,
        PlayModeSwitcher.OnModeChangeListener {

    private AutoSizeVideoView mVideoView;
    private TextView mBtnMute;
    private SeekBar mSeekBar;
    private ProgressBar mProgressBar;
    private PlayStateSwitcher mStateSwitcher;
    private PlayModeSwitcher mModeSwitcher;

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

    void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_play_controler, this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mStateSwitcher = (PlayStateSwitcher) findViewById(R.id.play_switch);
        mStateSwitcher.setOnStateChangeListener(this);
        mModeSwitcher = (PlayModeSwitcher) findViewById(R.id.state_s);
        mModeSwitcher.setOnModeChangeListener(this);
    }

    public void bindVideoView(AutoSizeVideoView videoView) {
        mVideoView = videoView;
    }

    public void onLoadingStart() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void onPlayingStart() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void onPreparedFinish() {
        mStateSwitcher.onStartPlay();
        findViewById(R.id.lv_control).setVisibility(VISIBLE);
    }

    private void onPlayComplete() {
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        mStateSwitcher.onPlayComplete();
    }

    private void mute(boolean mute) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mVideoView.seekTo(seekBar.getProgress());
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
    }

    @Override
    public void onPlayProgressUpdate(int percent) {
        if (percent != mSeekBar.getProgress()) {
            mSeekBar.setProgress(percent);
        }
    }

    @Override
    public void onPlayModeChanged(int playMode) {

    }

    @Override
    public void onBufferingUpdate(int percent) {
        mSeekBar.setSecondaryProgress(percent * mSeekBar.getMax() / 100);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mStateSwitcher.onPause();
        return false;
    }


    @Override
    public void onPlayClicked() {
        mVideoView.start();
    }

    @Override
    public void onPauseClicked() {
        mVideoView.pause();
    }

    @Override
    public void onModeChange(int mode) {
        mVideoView.switchState(mode);
    }
}
