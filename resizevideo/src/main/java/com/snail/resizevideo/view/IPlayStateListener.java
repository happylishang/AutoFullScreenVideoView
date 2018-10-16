package com.snail.resizevideo.view;

import android.media.MediaPlayer;
import android.widget.SeekBar;

/**
 * Author: snail
 * Data: 18-1-18 上午10:15
 * Des:
 * version:
 */

public interface IPlayStateListener extends SeekBar.OnSeekBarChangeListener {

    void onPlayStateChanged(int playState);

    void onPlayProgressUpdate(int percent);

    void onPlayModeChanged(int playMode);

    void onBufferingUpdate(int percent);

    boolean onError(MediaPlayer mp, int what, int extra);

    void onSeekComplete(MediaPlayer mp);

    void bindVideoView(IVideoView videoView);

    void setCoverImgUrl(String url);

    boolean isControlViewShowing();

    void setOnOuterActionListener(VideoPlayControlView.OnOuterActionListener onOuterActionListener);
}
