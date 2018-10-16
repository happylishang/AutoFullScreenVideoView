package com.snail.resizevideo.view;

public interface IVideoView {


    void start();

    void pause();

    void release();

    int getDuration();

    void seekTo(int progress);

    boolean isInPlaybackState();

    void switchPlayMode(int mode);

    boolean isPlaying();

    void mute();

    void cancelMute();

    int getCurrentState();

    int getCurrentMode();
}
