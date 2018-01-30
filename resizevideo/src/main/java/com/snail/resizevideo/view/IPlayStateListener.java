package com.snail.resizevideo.view;

import android.media.MediaPlayer;

/**
 * Author: snail
 * Data: 18-1-18 上午10:15
 * Des:
 * version:
 */

public interface IPlayStateListener {

    void onPlayStateChanged(int playState);

    void onPlayProgressUpdate(int percent);

    void onPlayModeChanged(int playMode);

    void onBufferingUpdate(int percent);

    boolean onError(MediaPlayer mp, int what, int extra);

}
