package androidx.ijk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.ijk.IJK;
import androidx.ijk.IJKOption;
import androidx.ijk.R;
import androidx.ijk.helper.IJKHelper;
import androidx.ijk.helper.OnIjkVideoTouchListener;
import androidx.ijk.helper.Orientation;
import androidx.ijk.listener.OnIJKVideoListener;
import androidx.ijk.listener.OnIJKVideoSwitchScreenListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.IjkTimedText;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Author: Relin
 * Describe:IJK视频播放器
 * Date:2020/5/11 17:32
 */
public class IJKVideoView extends FrameLayout implements TextureView.SurfaceTextureListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnTimedTextListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener, OnIjkVideoTouchListener, Cloneable {

    private final String TAG = IJKVideoView.class.getSimpleName();
    /**
     * 播放对象
     */
    private IjkMediaPlayer mediaPlayer;
    private IMediaPlayer iMediaPlayer;
    /**
     * 显示容器
     */
    private IJKTextureView textureView;
    /**
     * Video - surface
     */
    private Surface surface;
    /**
     * 视频加载Header
     */
    private Map<String, String> header;
    /**
     * 视频URI
     */
    private Uri uri;
    /**
     * 视频路径
     */
    private String path;
    /**
     * 视频监听
     */
    private OnIJKVideoListener onIJKVideoListener;
    /**
     * 视频控制View
     */
    private View controlView;
    /**
     * 控制器ViewHolder
     */
    private IJKControlViewHolder controlViewHolder;
    /**
     * 是否已经准备过
     */
    private boolean isPrepared;
    /**
     * 是否播放结束
     */
    private boolean isPlayEnd;
    /**
     * IJK助手
     */
    private IJKHelper ijkHelper;
    /**
     * 视频控件父容器
     */
    private ViewGroup container;
    /**
     * 最新图
     */
    private Bitmap bitmap;


    /**
     * 是否启用手势
     */
    public boolean useController = true;

    /**
     * 是否是直播数据源
     */
    private boolean liveSource;
    /**
     * 直播开始时间
     */
    private long liveStartTime = 0;

    /**
     * 屏幕切换监听
     *
     * @param context
     */
    private OnIJKVideoSwitchScreenListener onIJKVideoSwitchScreenListener;


    public IJKVideoView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public IJKVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IJKVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 设置是否是直播源
     *
     * @param liveSource
     */
    public void setLiveSource(boolean liveSource) {
        this.liveSource = liveSource;
        if (isLiveSource()) {
            controlViewHolder.getSeekBar().setEnabled(false);
            controlViewHolder.getSeekBar().setThumb(null);
        } else {
            controlViewHolder.getSeekBar().setEnabled(true);
            controlViewHolder.getSeekBar().setThumb(ContextCompat.getDrawable(getContext(), R.drawable.ijk_seek_dot));
        }
    }

    /**
     * 是否是直播源
     *
     * @return
     */
    public boolean isLiveSource() {
        return liveSource;
    }

    /**
     * 设置屏幕切换监听
     *
     * @param onIJKVideoSwitchScreenListener
     */
    public void setOnIJKVideoSwitchScreenListener(OnIJKVideoSwitchScreenListener onIJKVideoSwitchScreenListener) {
        this.onIJKVideoSwitchScreenListener = onIJKVideoSwitchScreenListener;
    }

    /**
     * 初始化播放器
     *
     * @param context 上下文
     * @param attrs   xml参数
     */
    private void init(Context context, AttributeSet attrs) {
        setBackgroundColor(Color.BLACK);
        ijkHelper = new IJKHelper(context, this, this);
        container = (ViewGroup) getParent();
        initMediaPlayer();
        initVideoSurface(context);
        initControlViews();
    }

    /**
     * 初始化媒体对象
     */
    public void initMediaPlayer() {
        // 禁用多点触控
        setMotionEventSplittingEnabled(false);
        // 初始化
        mediaPlayer = new IjkMediaPlayer();
        // 配置参数
        List<IJKOption> options = IJK.config().options();
        for (int i = 0; i < options.size(); i++) {
            IJKOption option = options.get(i);
            if (option.getValue() instanceof String) {
                mediaPlayer.setOption(option.getCategory(), option.getName(), (String) option.getValue());
            }
            if (option.getValue() instanceof Long) {
                mediaPlayer.setOption(option.getCategory(), option.getName(), (Long) option.getValue());
            }
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setScreenOnWhilePlaying(true);
        // 设置监听
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnVideoSizeChangedListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnTimedTextListener(this);
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoSeekEnable(true);
        }
    }

    /**
     * 初始化视频显示器
     *
     * @param context
     */
    public void initVideoSurface(Context context) {
        // 视频视图
        LayoutParams textureViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textureViewParams.gravity = Gravity.CENTER;
        if (textureView != null && getChildAt(0) instanceof TextureView) {
            removeView(textureView);
        }
        textureView = new IJKTextureView(context);
        textureView.setKeepScreenOn(true);
        addView(textureView, 0, textureViewParams);
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }
    }

    /**
     * 初始化控制器View
     */
    public void initControlViews() {
        // 控制器
        controlView = IJK.config().controlView();
        if (controlView == null) {
            if (IJK.config().controlLayoutId() == 0) {
                IJK.config().controlLayoutId(R.layout.ijk_video_control);
            }
            controlView = LayoutInflater.from(getContext()).inflate(IJK.config().controlLayoutId(), null);
            LayoutParams controlViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            controlViewParams.gravity = Gravity.BOTTOM;
            addView(controlView, controlViewParams);
        }
        controlViewHolder = new IJKControlViewHolder(this, controlView);
        controlViewHolder.findViews();

        controlViewHolder.hideController();
        // 中间控件隐藏
        // controlViewHolder.getCenterImageView().setVisibility(GONE);
        controlViewHolder.getVoiceLightProgressView().setVisibility(GONE);
        controlViewHolder.getLoadingView().setVisibility(GONE);
        controlViewHolder.getCoverImageView().setVisibility(GONE);
        // 底部播放按钮监听
        controlViewHolder.getPlayView().setOnClickListener(this);
        // 底部屏幕转换按钮监听
        controlViewHolder.getScreenSwitchView().setOnClickListener(this);
        // 中间播放按钮监听
        // controlViewHolder.getCenterImageView().setOnClickListener(this);
        // 进度条监听
        controlViewHolder.getSeekBar().setOnSeekBarChangeListener(this);
        // 是否直播
        controlView.setVisibility(isLiveSource() ? GONE : VISIBLE);
    }

    @Override
    public void onClick(View view) {
        // 底部播放、暂停 || 屏幕中间播放、暂停、重播
        if (view.getId() == R.id.iv_ijk_play /* || view.getId() == R.id.iv_ijk_center */) {
            controlPlay(isPlayEnd);
        }
        // 底部屏幕转换
        if (view.getId() == R.id.iv_ijk_screen) {
            this.toggleOrientation();
        }
    }

    /**
     * 改变旋转方向
     */
    public void toggleOrientation() {
        Orientation orientation;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            orientation = Orientation.LANDSCAPE;
        } else {
            orientation = Orientation.PORTRAIT;
        }
        ijkHelper.switchScreen(getContext(), this, orientation);
        if (onIJKVideoSwitchScreenListener != null) {
            onIJKVideoSwitchScreenListener.onIJKVideoSwitchScreen(orientation);
        }
    }

    //************************************[SeekBar监听]**************************************
    @SuppressLint("SetTextI18n")
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            /* mediaPlayer.seekTo(progress);
            long current = mediaPlayer.getCurrentPosition();
            boolean isForward = progress - current > 0;
            float percent = progress * 1f / seekBar.getMax();
            showCircleProgressPercent(percent, isForward ? ProgressType.FORWARD : ProgressType.BACKWARD); */

            long currentPos = mediaPlayer.getCurrentPosition();
            long vidDuration = mediaPlayer.getDuration();
            long difference = progress - currentPos;

            long newPos = progress;//(currentPos + difference);
            newPos = Math.max(0, newPos);
            newPos = Math.min(newPos, vidDuration);

            // 更新当前时间
            showVideoTime(newPos, controlViewHolder.getCurrentView());

            // 更新中间时间
            String text = ijkHelper.longToTimestamp(difference, false) + "[" + ijkHelper.longToTimestamp((long) newPos, true) + "]";
            getControlViewHolder().progressScrubberLayout.setVisibility(VISIBLE);
            getControlViewHolder().progressScrubberText.setText(text);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // seekBar 操作开始,设置不允许自动更新进度
        ijkHelper.setSwipeGestureProgressOpen(true);
        // 移除隐藏底部进度栏任务
        controlViewHolder.getProgressLayout().removeCallbacks(ijkHelper.getHideControllerAction());
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // seekBar 操作完成后,设置允许自动更新进度
        ijkHelper.setSwipeGestureProgressOpen(false);
        mediaPlayer.seekTo(seekBar.getProgress());
        // 五秒后隐藏底部栏
        if (controlViewHolder.getProgressLayout().getVisibility() == VISIBLE) {
            controlViewHolder.getProgressLayout().removeCallbacks(ijkHelper.getHideControllerAction());
            controlViewHolder.getProgressLayout().postDelayed(ijkHelper.getHideControllerAction(), 5000);
        }
        controlViewHolder.progressScrubberLayout.setVisibility(GONE);
    }

    /**
     * 设置播放源
     *
     * @param path
     */
    public void setDataSource(String path) {
        this.path = path;
        if (!TextUtils.isEmpty(path)) {
            setDataSource(Uri.parse(path));
        }
    }

    /**
     * 设置播放源
     *
     * @param uri
     */
    public void setDataSource(Uri uri) {
        setDataSource(uri, null);
    }

    public void setDataSource(IMediaDataSource dataSource) {
        mediaPlayer.setDataSource(dataSource);
    }

    /**
     * 设置播放源
     *
     * @param uri
     * @param header
     */
    public void setDataSource(Uri uri, Map<String, String> header) {
        this.uri = uri;
        if (uri != null) {
            if (header == null) {
                header = new HashMap<>();
            }
            header.put("Cache-Control", "no-store");
        }
        this.header = header;
        try {
            mediaPlayer.setDataSource(getContext(), uri, header);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 控制视频播放
     *
     * @param isPlayEnd
     */
    private void controlPlay(boolean isPlayEnd) {
        if (isPlayEnd) {
            restart();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                pause();
            } else {
                if (isPrepared) {
                    resume();
                } else {
                    start();
                }
            }
        }
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * 获取播放源
     *
     * @return
     */
    public String getDataSource() {
        return mediaPlayer.getDataSource();
    }

    /**
     * 获取视频时长
     *
     * @return
     */
    public long getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * 获取当前进度位置
     *
     * @return
     */
    public long getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * 进度控制
     *
     * @param msec
     */
    public void seekTo(long msec) {
        mediaPlayer.seekTo(msec);
    }

    /**
     * 将播放器设置为循环播放机或非循环播放机。
     *
     * @param looping
     */
    public void setLooping(boolean looping) {
        mediaPlayer.setLooping(looping);
    }

    /**
     * 设置显示器
     *
     * @param holder
     */
    public void setDisplay(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    /**
     * 保持屏幕持续点亮 --避免息屏
     *
     * @param keepInBackground
     */
    public void setKeepInBackground(boolean keepInBackground) {
        mediaPlayer.setKeepInBackground(keepInBackground);
    }

    /**
     * 重置播放器
     */
    public void reset() {
        mediaPlayer.reset();
    }

    /**
     * 停止播放
     */
    public void stop() {
        mediaPlayer.stop();
    }

    /**
     * 视频重新播放
     */
    public void restart() {
        showLoading();
        mediaPlayer.reset();
        mediaPlayer.setSurface(surface);
        controlViewHolder.getSeekBar().setProgress(0);
        controlViewHolder.getProgressBar().setProgress(0);
        showVideoTime(0, controlViewHolder.getCurrentView());
        showVideoTime(0, controlViewHolder.getDurationView());
        setDataSource(path);
        start();
    }

    /**
     * 视频暂停
     */
    public void pause() {
        controlViewHolder.getLoadingView().setVisibility(GONE);
        // controlViewHolder.getCenterImageView().setVisibility(VISIBLE);
        // controlViewHolder.getCenterImageView().setImageResource(R.drawable.ic_ijk_pause_control);
        controlViewHolder.getPlayView().setImageResource(R.drawable.ic_ijk_pause_control);
        mediaPlayer.pause();
        stopVideoProgress();
    }

    /**
     * 视频播放完毕
     */
    protected void onCompletion() {
        // controlViewHolder.getCenterImageView().setVisibility(VISIBLE);
        // controlViewHolder.getCenterImageView().setImageResource(R.mipmap.ic_ijk_replay);
        controlViewHolder.getPlayView().setImageResource(R.drawable.ic_ijk_pause_control);
        stopVideoProgress();
    }

    /**
     * 视频恢复播放
     */
    public void resume() {
        // controlViewHolder.getCenterImageView().setVisibility(GONE);
        controlViewHolder.getPlayView().setImageResource(R.drawable.ic_ijk_play_control);
        mediaPlayer.start();
        startVideoProgress();
    }

    /**
     * 异步准备
     */
    public void prepareAsync() {
        isPlayEnd = false;
        isPrepared = false;
        mediaPlayer.prepareAsync();
    }

    /**
     * 开始播放
     */
    public void start() {
        showLoading();
        isPlayEnd = false;
        isPrepared = false;
        // controlViewHolder.getCenterImageView().setVisibility(GONE);
        mediaPlayer.prepareAsync();
    }

    /**
     * 释放播放器
     */
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    /**
     * 销毁资源
     */
    public void destroy() {
        release();
        AudioManager am = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(null);
        stopVideoProgress();
        IjkMediaPlayer.native_profileEnd();
    }


    /**
     * 获取控制器View
     *
     * @return
     */
    public View getControlView() {
        return controlView;
    }

    /**
     * 获取控制器ViewHolder
     *
     * @return
     */
    public IJKControlViewHolder getControlViewHolder() {
        return controlViewHolder;
    }

    /**
     * 设置视频监听
     *
     * @param onIJKVideoListener
     */
    public void setOnIJKVideoListener(OnIJKVideoListener onIJKVideoListener) {
        this.onIJKVideoListener = onIJKVideoListener;
    }

    /**
     * 获取视频监听
     *
     * @return
     */
    public OnIJKVideoListener getOnIJKVideoListener() {
        return onIJKVideoListener;
    }

    /**
     * 获取播放器控制对象
     *
     * @return
     */
    public IjkMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    /**
     * 获取视频路径
     *
     * @return 视频路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 获取视频URI地址
     *
     * @return 视频URI地址
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * 获取显示器对象
     *
     * @return 显示器对象
     */
    public IJKTextureView getTextureView() {
        return textureView;
    }

    /**
     * @return .
     */
    public SurfaceTexture getSurfaceTexture() {
        return textureView.getSurfaceTexture();
    }

    /**
     * 获取视频显示器
     *
     * @return .
     */
    public Surface getSurface() {
        return surface;
    }

    /**
     * 设置视频显示器
     *
     * @param surface .
     */
    public void setSurface(Surface surface) {
        this.surface = surface;
        if (mediaPlayer != null) {
            mediaPlayer.setSurface(surface);
        }
    }

    /**
     * 设置播放期间屏幕常亮
     *
     * @param screenOn .
     */
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mediaPlayer != null) {
            mediaPlayer.setScreenOnWhilePlaying(screenOn);
        }
    }

    /**
     * 获取容器
     *
     * @return .
     */
    public ViewGroup getContainer() {
        return container;
    }

    //****************************************[TextureView - SurfaceTextureListener]**********************************************
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable " + width + "," + height);
        surface = new Surface(surfaceTexture);
        controlViewHolder.getCoverImageView().setVisibility(GONE);
        setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.i(TAG, "onSurfaceTextureSizeChanged " + width + "," + height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        surface.release();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        bitmap = textureView.getBitmap();
        // Log.i(TAG, "onSurfaceTextureUpdated");
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            controlViewHolder.getCoverImageView().setVisibility(GONE);
        }
    }

    //****************************************[TextureView - SurfaceTextureListener]**********************************************
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onPrepared");
        this.iMediaPlayer = iMediaPlayer;
        this.isPrepared = true;
        if (isLiveSource()) {
            liveStartTime = System.currentTimeMillis();
        }
        if (surface != null) {
            iMediaPlayer.setSurface(surface);
        }
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoPrepared(iMediaPlayer);
        }
        // 视频准备好之后更新底部进度条数据
        onVideoProgress(iMediaPlayer, iMediaPlayer.getDuration(), 0);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        Log.i(TAG, "onCompletion");
        isPlayEnd = true;
        onCompletion();
        this.iMediaPlayer = iMediaPlayer;
        // 播放完毕，进度条满格
        showVideoTime(iMediaPlayer.getDuration(), controlViewHolder.getCurrentView());
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoCompletion(iMediaPlayer);
        }
    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {
        this.iMediaPlayer = iMediaPlayer;
        Log.i(TAG, "onSeekComplete");
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoSeekComplete(iMediaPlayer);
        }
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int width, int height, int sarNum, int sarDen) {
        this.iMediaPlayer = iMediaPlayer;
        if (textureView != null) {
            textureView.setVideoSize(width, height);
        }
        Log.i(TAG, "onVideoSizeChanged width:" + width + ",height:" + height + ",sarNum：" + sarNum + ",sarDen:" + sarDen);
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoSizeChanged(iMediaPlayer, width, height, sarNum, sarDen);
        }
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
        dismissLoading();
        this.iMediaPlayer = iMediaPlayer;
        Log.i(TAG, "onError framework_err:" + framework_err + ",impl_err:" + impl_err);
        if (onIJKVideoListener != null) {
            onIJKVideoListener.onVideoError(iMediaPlayer, framework_err, impl_err);
        }
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int args) {
        this.iMediaPlayer = iMediaPlayer;
        Log.i(TAG, "onInfo what:" + what + ",args:" + args);
        if (what == IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            controlViewHolder.getPlayView().setImageResource(R.drawable.ic_ijk_play_control);
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoRenderingStart(iMediaPlayer, args);
            }
            // 消失封面
            controlViewHolder.getCoverImageView().setVisibility(GONE);
            // 消失Loading
            dismissLoading();
            // 开始计时
            startVideoProgress();
            // 如不是自动播放暂停
            if (!IJK.config().isAutoPlay()) {
                pause();
            }
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoSeekEnable(false);
            }
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            showLoading();
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoBufferingStart(iMediaPlayer, args);
            }
            stopVideoProgress();
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            // 消失封面
            controlViewHolder.getCoverImageView().setVisibility(GONE);
            // 消失加载
            dismissLoading();
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoBufferingEnd(iMediaPlayer, args);
            }
            startVideoProgress();
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED) {
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoRotationChanged(iMediaPlayer, args);
            }
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoTrackLagging(iMediaPlayer, args);
            }
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {
            dismissLoading();
            if (onIJKVideoListener != null) {
                onIJKVideoListener.onVideoBadInterleaving(iMediaPlayer, args);
            }
        }
        return false;
    }

    @Override
    public void onTimedText(IMediaPlayer mp, IjkTimedText text) {
        Log.i(TAG, "onTimedText " + text.getText());
    }

    /**
     * 开始进度获取标识
     */
    private static final int WHAT_GET_DURATION = 0;

    /**
     * 开始进度获取
     */
    private void startVideoProgress() {
        if (durationHandler != null) {
            durationHandler.sendEmptyMessage(WHAT_GET_DURATION);
        }
    }

    /**
     * 停止进度获取
     */
    private void stopVideoProgress() {
        if (durationHandler != null) {
            durationHandler.removeMessages(WHAT_GET_DURATION);
        }
    }

    /**
     * 时长Handler todo 这里需要处理内存泄漏
     */
    @SuppressLint("HandlerLeak")
    private final Handler durationHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_GET_DURATION) {
                long duration = isLiveSource() ? 0 : iMediaPlayer.getDuration();
                liveStartTime = liveStartTime == 0 ? System.currentTimeMillis() : liveStartTime;
                long current = isLiveSource() ? System.currentTimeMillis() - liveStartTime : iMediaPlayer.getCurrentPosition();
                Log.i(TAG, "onVideoProgress duration=" + duration + ",current=" + current + ",isLiveSource=" + isLiveSource());
                // 如果正在收到更新进度那么不自动更新进度
                if (!ijkHelper.getSwipeGestureProgressOpen()) {
                    onVideoProgress(iMediaPlayer, duration, current);
                }
                if (onIJKVideoListener != null) {
                    onIJKVideoListener.onVideoProgress(iMediaPlayer, iMediaPlayer.getDuration(), iMediaPlayer.getCurrentPosition());
                }
                sendEmptyMessageDelayed(WHAT_GET_DURATION, 1000);
            }
        }
    };

    /**
     * 视频进度
     *
     * @param iMediaPlayer 视频对象
     * @param duration     时长
     * @param current      当前进度
     */
    protected void onVideoProgress(IMediaPlayer iMediaPlayer, long duration, long current) {
        controlViewHolder.getSeekBar().setMax((int) duration);
        controlViewHolder.getSeekBar().setProgress((int) current);
        controlViewHolder.getProgressBar().setMax((int) duration);
        controlViewHolder.getProgressBar().setProgress((int) current);
        showVideoTime(current, controlViewHolder.getCurrentView());
        showVideoTime(duration, controlViewHolder.getDurationView());
    }

    /**
     * 显示视频时间
     *
     * @param time   时间
     * @param tvShow 控件
     */
    public static void showVideoTime(long time, TextView tvShow) {
        DecimalFormat format = new DecimalFormat("00");
        long second = time / 1000;
        long hour = second / 60 / 60;
        String timeText;
        if (hour > 0) {
            long videoMinutes = (second - hour * 3600) / 60;
            long videoSecond = second % 60;
            timeText = format.format(hour) + ":" + format.format(videoMinutes) + ":" + format.format(videoSecond);
        } else {
            long videoSecond = second % 60;
            long videoMinutes = second / 60;
            timeText = format.format(videoMinutes) + ":" + format.format(videoSecond);
        }
        tvShow.setText(timeText);
    }

    /**
     * 显示Loading
     */
    public void showLoading() {
        // 加载中隐藏播放按钮,显示加载中按钮
        controlViewHolder.getPlayView().setVisibility(View.GONE);
        controlViewHolder.getLoadingView().setVisibility(VISIBLE);
        AnimationDrawable drawable = (AnimationDrawable) controlViewHolder.getLoadingImageView().getBackground();
        drawable.start();
    }

    /**
     * 消失Loading
     */
    public void dismissLoading() {
        // 加载完成后,播放按钮展示状态和进度一致
        controlViewHolder.getPlayView().setVisibility(controlViewHolder.getProgressLayout().getVisibility());
        controlViewHolder.getLoadingView().setVisibility(GONE);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, "onInterceptTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, "onInterceptTouchEvent ACTION_UP");
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    //*******************************[onTouchEvent]*********************************
    @Override
    public void onVideoChangeBrightness(float value, float percent) {
        ijkHelper.changeBrightness(getContext(), value);
        showCircleProgressPercent(percent, ProgressType.BRIGHTNESS);
    }

    @Override
    public void onVideoChangeVoice(float value, float percent) {
        ijkHelper.changeVoice(getContext(), value);
        showCircleProgressPercent(percent, ProgressType.VOICE);
    }

    @Override
    public void onVideoStartChangeProgress(long value, float percent) {
        long current = mediaPlayer.getCurrentPosition();
        boolean isForward = value - current > 0;
        controlViewHolder.getSeekBar().setProgress((int) value);
        controlViewHolder.getProgressBar().setProgress((int) value);
        showCircleProgressPercent(percent, isForward ? ProgressType.FORWARD : ProgressType.BACKWARD);
    }

    @Override
    public void onVideoStopChangeProgress(long value, float percent) {
        mediaPlayer.seekTo(value);
        controlViewHolder.getSeekBar().setProgress((int) value);
        controlViewHolder.getProgressBar().setProgress((int) value);
    }

    @Override
    public void onVideoControlViewShow(MotionEvent event) {

    }

    @Override
    public void onVideoControlViewHide(MotionEvent event) {
        controlViewHolder.getVoiceLightProgressView().setVisibility(GONE);
    }

    public enum ProgressType {
        VOICE, BRIGHTNESS, FORWARD, BACKWARD
    }

    /**
     * 显示声音亮度百分比
     *
     * @param percent 百分比[0-1]
     * @param type    进度类型
     */
    private void showCircleProgressPercent(float percent, ProgressType type) {
        controlViewHolder.getVoiceLightProgressView().setVisibility(VISIBLE);
        String progressText = "";
        if (type == ProgressType.VOICE) {
            progressText = "音";
        }
        if (type == ProgressType.BRIGHTNESS) {
            progressText = "亮";
        }
        if (type == ProgressType.FORWARD) {
            progressText = "进";
        }
        if (type == ProgressType.BACKWARD) {
            progressText = "退";
        }
        controlViewHolder.getVoiceLightProgressView().setProgressText(progressText);
        controlViewHolder.getVoiceLightProgressView().setMax(100);
        controlViewHolder.getVoiceLightProgressView().setProgress((int) (percent * 100f));
    }

    /**
     * 是否播放结束
     *
     * @return 是否播放结束
     */
    public boolean isPlayEnd() {
        return isPlayEnd;
    }

    /**
     * 是否已经准备好
     *
     * @return 是否已经准备好
     */
    public boolean isPrepared() {
        return isPrepared;
    }
}
