package com.snail.resizevideo.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.SeekBar;

/**
 * Author: snail
 * Data: 2018/10/9.
 * Des:
 * version:
 */
public class BaseVideoPlayControlView extends FrameLayout implements IPlayStateListener {

    public BaseVideoPlayControlView(@NonNull Context context) {
        super(context);
    }

    public BaseVideoPlayControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseVideoPlayControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onPlayStateChanged(int playState) {

    }

    @Override
    public void onPlayProgressUpdate(int percent) {

    }

    @Override
    public void onPlayModeChanged(int playMode) {

    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void bindVideoView(IVideoView videoView) {

    }

    @Override
    public void setCoverImgUrl(String url) {

    }

    @Override
    public boolean isControlViewShowing() {
        return false;
    }

    @Override
    public void setOnOuterActionListener(VideoPlayControlView.OnOuterActionListener onOuterActionListener) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    // 目前埋点统计用先写这几个
    public interface OnOuterActionListener {

        void onPlayStart(); //开始播放

        void onPauseClicked();//点击暂停

        void onCompleteOnce();//完成一次播放

        void onEnterFullScreen();//点击进入全屏

        void onHideControlViews(int mode);//隐藏控制器

        void onShowControlViews(int mode);//显示控制器

        void onNoWifiPlayClicked();//4G情况下，点击播放
    }

}
