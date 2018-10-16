package com.snail.resizevideo.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import utils.NetworkUtil;

import static android.media.AudioManager.STREAM_MUSIC;


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
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnSeekCompleteListener,
        IVideoView {

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
    private int mTargetState = PlayState.IDLE;
    private int mLength;
    private static final int TOTAL_PERCENT = 1000;
    private static final int UPDATE_FREQUENCY = 100;
    private Timer mTimer;
    private ScreenStatusReceiver mScreenStatusReceiver = new ScreenStatusReceiver();
    private FrameLayout mContainer;
    private IPlayStateListener mVideoPlayControlView;
    private boolean mHasPreLoaded = false;
    private boolean mHasReset = false;
    private String mVideoSize;
    private boolean mLooping;
    private boolean mCanScale;

    public void setCanScale(boolean canScale) {
        mCanScale = canScale;
    }


    public interface PlayMode {
        int NORMAL = 1; //普通
        int LANDSCAPE_FULL = 2;//横向全屏
    }

    public boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mSurfaceTexture != null &&
                mCurrentState != PlayState.ERROR &&
                mCurrentState != PlayState.IDLE &&
                mCurrentState != PlayState.PREPARING);
    }

    public boolean isInFullScreen() {
        return mCurrentMode == PlayMode.LANDSCAPE_FULL;
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
    }

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

    private void init() {
        mContainer = new FrameLayout(getContext());
        addView(mContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mTextureView = new TextureView(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(mTextureView, layoutParams);
        mTextureView.setSurfaceTextureListener(this);
        mMediaPlayer = new MediaPlayer();
        initCallBackListeners();
    }

    public void addVideoPlayControlView(BaseVideoPlayControlView controlView) {
        mVideoPlayControlView = controlView;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.BOTTOM;
        addView(controlView, params);
        mVideoPlayControlView.bindVideoView(this);
    }

    private void markVideoParams(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        if (mVideoWidth > 0) {
            mTextureView.getLayoutParams().height = getResources().getDisplayMetrics().widthPixels * mVideoHeight / mVideoWidth;
        }
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

    private android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper());

    /***
     * 刷新播放进度
     */
    private void startTicks() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mMediaPlayer != null
                                    && mCurrentState == PlayState.PLAYING
                                    && mMediaPlayer.getDuration() > 0) {
                                int duration = mMediaPlayer.getDuration();
                                if (duration > 0) {
                                    mVideoPlayControlView.onPlayProgressUpdate(
                                            TOTAL_PERCENT * mMediaPlayer.getCurrentPosition() / duration);
                                }
                            }
                        }
                    });

                }
            }, 0, UPDATE_FREQUENCY);
        }
    }

    public void setUrl(String url, String size) {
        mVideoSize = size;
        mUrl = url;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    /**
     * 暂存外部的容器（全屏用Activity的维度）
     * 为了通用，直接在Activity层添加回调
     */
    public void bindOutContainerActivity(IFullScreenVideoContainer activity) {
        if (activity instanceof Activity) {
            mOutContainer = (ViewGroup) ((Activity) activity).findViewById(android.R.id.content);
        }
        activity.bindFullScreenVideoView(this);
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public int getCurrentMode() {
        return mCurrentMode;
    }

    public void switchPlayMode(int state) {

        if (mOutContainer == null) {
            return;
        }
        switch (state) {
            case PlayMode.LANDSCAPE_FULL:
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
        if (isInPlaybackState()) {
            mCurrentState = PlayState.PLAYING;
            mVideoPlayControlView.onPlayStateChanged(mCurrentState);
            startTicks();
            mMediaPlayer.setLooping(mLooping);
            mMediaPlayer.start();
            return;
        }
        mTargetState = PlayState.PLAYING;
        if (mCurrentState == PlayState.ERROR && NetworkUtil.isNetworkOpened(getContext())) {
            reStart();
            return;
        }
        if (mCurrentState == PlayState.IDLE || mHasReset) {
            loadVideoSource();
        }
    }


    public void pause() {

        if (mCurrentState == PlayState.IDLE) {
            return;
        }
        if (mCurrentState == PlayState.PLAYING || mCurrentState == PlayState.BUFFERING_PLAYING) {
            mMediaPlayer.pause();
        }
        mTargetState = PlayState.PAUSED;
        mVideoPlayControlView.onPlayStateChanged(mTargetState);
        if (!isInPlaybackState() && mHasPreLoaded) {
            mHasReset = true;
            mCurrentState = PlayState.IDLE;
            mMediaPlayer.reset();
        } else {
            mCurrentState = PlayState.PAUSED;
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
        mTargetState = PlayState.IDLE;
        mCurrentState = PlayState.IDLE;
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
    }

    private void loadVideoSource() {
        if (TextUtils.isEmpty(mUrl)) {
            return;
        }
        try {
            mMediaPlayer.setDataSource(mUrl);
            mMediaPlayer.setAudioStreamType(STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mCurrentState = PlayState.PREPARING;
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setVolume(0, 0);
            mVideoPlayControlView.onPlayStateChanged(mCurrentState);
            mHasPreLoaded = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void seekTo(int progress) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(progress * mLength / TOTAL_PERCENT);
        }
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    //    视频未做精确处理 假定视频宽》高
    void adjustLayoutParams(int state) {

        if (mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        DisplayMetrics dm = getContext().getApplicationContext().getResources().getDisplayMetrics();
        int realScreenWidth = dm.widthPixels > dm.heightPixels ? dm.heightPixels : dm.widthPixels;
        switch (state) {
            case PlayMode.LANDSCAPE_FULL:
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ((Activity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTextureView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                mTextureView.getLayoutParams().width = realScreenWidth * mVideoWidth / mVideoHeight;
                mTextureView.requestLayout();
                break;
            case PlayMode.NORMAL:
                ((Activity) getContext()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTextureView.getLayoutParams().height = realScreenWidth * mVideoHeight / mVideoWidth;
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
            if (mTargetState == PlayState.PLAYING) {
                start();
            }
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
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mCurrentState == PlayState.PLAYING) {
            mMediaPlayer.seekTo(0);
            mCurrentState = PlayState.COMPLETED;
            mTargetState = PlayState.COMPLETED;
            mVideoPlayControlView.onPlayStateChanged(mCurrentState);
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mVideoPlayControlView.onBufferingUpdate(percent);
    }

    private void reStart() {
        if (isInPlaybackState()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
        loadVideoSource();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {


        mCurrentState = PlayState.ERROR;
        mVideoPlayControlView.onSeekComplete(mp);
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
            case MediaPlayer.MEDIA_ERROR_IO:
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                break;
        }
        mVideoPlayControlView.onError(mp, what, extra);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        markVideoParams(mp.getVideoWidth(), mp.getVideoHeight());
        mCurrentState = PlayState.PREPARED;
        mVideoPlayControlView.onPlayStateChanged(mCurrentState);
        if (mTargetState == PlayState.PLAYING) {
            start();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mVideoPlayControlView.onSeekComplete(mp);
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
                mVideoPlayControlView.onPlayStateChanged(mCurrentState);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mCurrentState == PlayState.BUFFERING_PLAYING) {
                    mCurrentState = PlayState.PLAYING;
                }
                mVideoPlayControlView.onPlayStateChanged(mCurrentState);
                break;
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
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) && isInPlaybackState()) {
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
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(mNetworkReceiver, filter);
    }

    /**
     * 移除监听
     */
    @Override
    protected void onDetachedFromWindow() {
        getContext().unregisterReceiver(mScreenStatusReceiver);
        getContext().unregisterReceiver(mNetworkReceiver);
        super.onDetachedFromWindow();
    }

    public void exitFullScreen() {
        if (mCurrentMode == PlayMode.LANDSCAPE_FULL) {
            switchPlayMode(PlayMode.NORMAL);
            mVideoPlayControlView.onPlayModeChanged(PlayMode.NORMAL);
        }
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mCurrentState == PlayState.PLAYING;
    }

    public void setLooping(boolean needLoop) {
        mLooping = needLoop;
    }

    public boolean isLooping() {
        return mLooping;
    }

    /**
     * 静音
     */
    public void mute() {
        if (mMediaPlayer != null) {
            AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            am.requestAudioFocus(null, STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mMediaPlayer.setVolume(0, 0);
        }
    }

    /**
     * 取消静音
     */
    public void cancelMute() {
        if (mMediaPlayer == null) {
            return;
        }
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.requestAudioFocus(null, STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        mMediaPlayer.setAudioStreamType(STREAM_MUSIC);
        float volume = 0;
        if (am != null) {
            volume = am.getStreamMaxVolume(STREAM_MUSIC) * 1.0f / am.getStreamMaxVolume(STREAM_MUSIC);
        }
        mMediaPlayer.setVolume(volume, volume);
    }

    /**
     * 网络状态变化监听
     */
    BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
                //当前有网络, 但是网络发生了切换
                if (!NetworkUtil.isNetworkOpened(getContext())) {
                    if (mCurrentState == PlayState.PREPARING) {
                        Toast.makeText(getContext(), "网络不可用", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    public IPlayStateListener getVideoPlayControlView() {
        return mVideoPlayControlView;
    }

    //center_crop裁剪
    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        if (!mCanScale || viewWidth == 0 || viewHeight == 0 || mVideoWidth == 0 || mVideoHeight == 0) {
            return;
        }
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        if (mVideoWidth > viewWidth && mVideoHeight > viewHeight) {
            scaleX = mVideoWidth / viewWidth;
            scaleY = mVideoHeight / viewHeight;
        } else if (mVideoWidth < viewWidth && mVideoHeight < viewHeight) {
            scaleY = viewWidth / mVideoWidth;
            scaleX = viewHeight / mVideoHeight;
        } else if (viewWidth > mVideoWidth) {
            scaleY = (viewWidth / mVideoWidth) / (viewHeight / mVideoHeight);
        } else if (viewHeight > mVideoHeight) {
            scaleX = (viewHeight / mVideoHeight) / (viewWidth / mVideoWidth);
        }
        int pivotPointX = viewWidth / 2;
        int pivotPointY = viewHeight / 2;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleX, scaleY, pivotPointX, pivotPointY);
        mTextureView.setTransform(matrix);
        mTextureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }
}
