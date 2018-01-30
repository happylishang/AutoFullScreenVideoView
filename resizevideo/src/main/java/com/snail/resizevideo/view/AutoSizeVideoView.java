package com.snail.resizevideo.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Author: snail
 * Data: 18-1-18 上午10:17
 * Des: Mediaplayer+TextureView (TextureView比较好控制，并且不黑屏，为了方便控制切换的连续性，不方便用VideoView)
 * version:
 */


public class AutoSizeVideoView extends FrameLayout implements
        TextureView.SurfaceTextureListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener {

    private ViewGroup mOriginContainer;
    private ViewGroup mOutContainer;
    private String mUrl;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private int mVideoWidth;
    private int mVideoHeight;
    private SurfaceTexture mSurfaceTexture;
    private int mCurrentMode = PlayMode.NORMAL;
    private int mCurrentState = PlayState.IDLE;
    private int mLength;
    private static final int TOTAL_PERCENT = 1000;
    private static final int UPDATE_FREQUENCY = 100;
    private Timer mTimer;
    private ScreenStatusReceiver mScreenStatusReceiver = new ScreenStatusReceiver();

    public interface PlayMode {

        int NORMAL = 1; //普通
        int VERTICAL_FULL = 2;//垂直全屏
        int LANDSCAPE_FULL = 3;//横向全屏
    }

    public interface PlayState {
        int ERROR = -1;
        int IDLE = 0;
        int PREPARING = 1;
        int PREPARED = 2;
        int PLAYING = 3;
        int PAUSED = 4;
        int BUFFERING_PLAYING = 5;
        int COMPLETED = 6;
        int INTENET_ERROR = 7;
    }

    public void setmOnPlayStateChangeListener(IPlayStateListener onPlayStateChangeListener) {
        this.mOnPlayStateChangeListener = onPlayStateChangeListener;
    }

    private IPlayStateListener mOnPlayStateChangeListener;


    public AutoSizeVideoView(@NonNull Context context) {
        this(context, null);
    }

    public AutoSizeVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSizeVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private FrameLayout mContainer;

    private void init() {
        mContainer = new FrameLayout(getContext());
        addView(mContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTextureView = new TextureView(getContext());
        mContainer.addView(mTextureView);
        mTextureView.setSurfaceTextureListener(this);
        mMediaPlayer = new MediaPlayer();
        initCallBackListeners();
    }

    private void markVideoParams(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        mTextureView.getLayoutParams().height = getResources().getDisplayMetrics().widthPixels * mVideoHeight / mVideoWidth;
        mTextureView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        mTextureView.requestLayout();
        mLength = mMediaPlayer.getDuration();
    }


    private void initCallBackListeners() {
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnInfoListener(this);
    }


    private void startTicks() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mOnPlayStateChangeListener != null &&
                            mCurrentState == PlayState.PLAYING
                            && mMediaPlayer.getDuration() > 0) {
                        mOnPlayStateChangeListener.onPlayProgressUpdate(TOTAL_PERCENT * mMediaPlayer.getCurrentPosition() / mMediaPlayer.getDuration());
                    }
                }
            }, 0, UPDATE_FREQUENCY);
        }
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    /**
     * 暂存原来的容器
     */
    public void registerOriginContainer(ViewGroup container) {
        mOriginContainer = container;
    }

    /**
     * 暂存外部的容器（全屏用Activity的维度）
     */
    public void registerOutContainer(IFullScreenVideoContainer fullScreenVideo) {
        mOutContainer = fullScreenVideo.getOutContainer();
    }

    public int getCurrentState() {
        return mCurrentMode;
    }


    public void switchState(int state) {

        if (mOutContainer == null || mOriginContainer == null) {
            return;
        }
        switch (state) {
            case PlayMode.LANDSCAPE_FULL:
                ((ViewGroup) mContainer.getParent()).removeView(mContainer);
                mOutContainer.addView(mContainer);
                break;
            case PlayMode.VERTICAL_FULL:
                ((ViewGroup) mContainer.getParent()).removeView(mContainer);
                mOutContainer.addView(mContainer);
                break;
            case PlayMode.NORMAL:
                ((ViewGroup) mContainer.getParent()).removeView(mContainer);
                addView(mContainer);
                break;
            default:
                break;
        }
        mCurrentMode = state;
        adjustLayoutParams(state);
    }


    public void start() {
        startTicks();
        switch (mCurrentState) {
            case PlayState.COMPLETED:
            case PlayState.PAUSED:
            case PlayState.BUFFERING_PLAYING:
            case PlayState.PLAYING:
                mCurrentState = PlayState.PLAYING;
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
                }
                mMediaPlayer.start();
                return;
        }
        if (mSurfaceTexture != null) {
            loadVideoSource();
        } else {
            // 防止调用时机过早
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadVideoSource();
                }
            }, 300);
        }
        setKeepScreenOn(true);
    }

    public void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        switch (mCurrentState) {
            case PlayState.PAUSED:
            case PlayState.BUFFERING_PLAYING:
            case PlayState.PLAYING:
                mMediaPlayer.pause();
            default:
                break;
        }
        mCurrentState = PlayState.PAUSED;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
        }
    }

    public void resume() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }


    public void release() {
        mSurface = null;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
        mTextureView = null;
    }

    private void loadVideoSource() {
        try {
            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mCurrentState = PlayState.PREPARING;
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress * mLength / TOTAL_PERCENT);
    }

    void adjustLayoutParams(int state) {

        DisplayMetrics dm = getContext().getApplicationContext().getResources().getDisplayMetrics();
        int realWidth = dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels;
        switch (state) {
            case PlayMode.LANDSCAPE_FULL:
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((Activity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTextureView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                mTextureView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                mTextureView.requestLayout();
                break;
            case PlayMode.VERTICAL_FULL:
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mTextureView.getLayoutParams().height = realWidth * mVideoHeight / mVideoWidth;
                mTextureView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                mTextureView.requestLayout();
                break;
            case PlayMode.NORMAL:
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                mTextureView.getLayoutParams().height = realWidth * mVideoHeight / mVideoWidth;
                mTextureView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                mTextureView.requestLayout();
                break;
            default:
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surfaceTexture;
            mSurface = new Surface(surfaceTexture);
            mMediaPlayer.setSurface(mSurface);
        } else {
            mTextureView.setSurfaceTexture(mSurfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return mSurfaceTexture == null;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        setKeepScreenOn(false);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        mMediaPlayer.seekTo(0);
        mCurrentState = PlayState.COMPLETED;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onBufferingUpdate(percent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onError(mp, what, extra);
            if (mCurrentState == PlayState.PLAYING) {
                pause();
            }
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        markVideoParams(mp.getVideoWidth(), mp.getVideoHeight());
        mCurrentState = PlayState.PREPARED;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        switch (what) {
            case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mCurrentState = PlayState.PLAYING;
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mCurrentState == PlayState.PLAYING) {
                    mCurrentState = PlayState.BUFFERING_PLAYING;
                }
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mCurrentState == PlayState.BUFFERING_PLAYING) {
                    mCurrentState = PlayState.PLAYING;
                }
                break;
        }
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(mCurrentState);
        }
        return false;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) {
            pause();
        }
    }

    /**
     * 关闭屏幕的时候，暂停播放
     */
    class ScreenStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                pause();
            }
        }
    }

    /**
     * 添加监听
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getContext().registerReceiver(mScreenStatusReceiver, filter);
    }

    /**
     * 移除监听
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        release();
        getContext().unregisterReceiver(mScreenStatusReceiver);
    }

}
