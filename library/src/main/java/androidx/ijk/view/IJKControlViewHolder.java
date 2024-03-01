package androidx.ijk.view;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.ijk.R;

import org.jetbrains.annotations.NotNull;


/**
 * Author: Relin
 * Describe:控制器ViewHolder
 * Date:2020/5/12 18:24
 */
public class IJKControlViewHolder {

    private ImageView imageFfwdAnimationRipple;
    private ImageView imageRewindAnimationRipple;
    private ImageView imagePlaybackAnimationRipple;

    /**
     * 父视图
     */
    private View parent;
    /**
     * 控制器View
     */
    private View controlView;
    /**
     * 播放/暂停
     */
    private ImageView iv_ijk_play;

    private View progress_layout;
    /**
     * 当前时间
     */
    private TextView tv_ijk_current;
    /**
     * 进度
     */
    private SeekBar seek_ijk_bar;
    private ProgressBar progress_bar;
    /**
     * 时长
     */
    private TextView tv_ijk_duration;
    /**
     * 屏幕转换
     */
    private ImageView iv_ijk_screen;
    /**
     * 屏幕中间控件
     */
    // private ImageView iv_ijk_center;
    /**
     * 屏幕中间进度条
     */
    private IJKCircleProgressView ijk_center_progress;
    /**
     * loading
     */
    private FrameLayout fl_ijk_loading;
    /**
     * ImageView Loading
     */
    private ImageView iv_ijk_loading;
    /**
     * 封面
     */
    private ImageView iv_ijk_cover;

    /**
     * 音量
     */
    public View gestureVolumeLayout;
    public ProgressBar gestureVolumeProgressBar;
    public TextView gestureVolumeText;
    public ImageView gestureVolumeImage;

    /**
     * 亮度
     */
    public View gestureBrightnessLayout;
    public ProgressBar gestureBrightnessProgressBar;
    public TextView gestureBrightnessText;
    public ImageView gestureBrightnessImage;

    /**
     * 进度
     */
    public View progressScrubberLayout;
    public TextView progressScrubberText;


    public IJKControlViewHolder(View parent, View controlView) {
        this.parent = parent;
        this.controlView = controlView;
    }

    /**
     * 找到所有控件
     */
    public void findViews() {
        iv_ijk_play = controlView.findViewById(R.id.iv_ijk_play);
        progress_layout = controlView.findViewById(R.id.progress_layout);
        tv_ijk_current = controlView.findViewById(R.id.tv_ijk_current);
        seek_ijk_bar = controlView.findViewById(R.id.seek_ijk_bar);
        progress_bar = controlView.findViewById(R.id.progress_bar);
        tv_ijk_duration = controlView.findViewById(R.id.tv_ijk_duration);
        iv_ijk_screen = controlView.findViewById(R.id.iv_ijk_screen);
        // iv_ijk_center = controlView.findViewById(R.id.iv_ijk_center);
        ijk_center_progress = controlView.findViewById(R.id.ijk_center_progress);
        fl_ijk_loading = controlView.findViewById(R.id.fl_ijk_loading);
        iv_ijk_loading = controlView.findViewById(R.id.iv_ijk_loading);
        iv_ijk_cover = controlView.findViewById(R.id.iv_ijk_cover);

        // 音量
        gestureVolumeLayout = controlView.findViewById(R.id.gesture_volume_layout);
        gestureVolumeProgressBar = controlView.findViewById(R.id.gesture_volume_progress_bar);
        gestureVolumeText = controlView.findViewById(R.id.gesture_volume_text);
        gestureVolumeImage = controlView.findViewById(R.id.gesture_volume_image);
        // 亮度
        gestureBrightnessLayout = controlView.findViewById(R.id.gesture_brightness_layout);
        gestureBrightnessProgressBar = controlView.findViewById(R.id.gesture_brightness_progress_bar);
        gestureBrightnessText = controlView.findViewById(R.id.gesture_brightness_text);
        gestureBrightnessImage = controlView.findViewById(R.id.gesture_brightness_image);

        // 进度
        progressScrubberLayout = controlView.findViewById(R.id.progress_scrubber_layout);
        progressScrubberText = controlView.findViewById(R.id.progress_scrubber_text);

        // 动画
        imageFfwdAnimationRipple = controlView.findViewById(R.id.image_ffwd_animation_ripple);
        imageRewindAnimationRipple = controlView.findViewById(R.id.image_rewind_animation_ripple);
        imagePlaybackAnimationRipple = controlView.findViewById(R.id.image_playback_animation_ripple);
    }

    /**
     * 获取父级视图
     *
     * @return
     */
    public View getParentView() {
        return parent;
    }

    /**
     * 获取控制View
     *
     * @return
     */
    public View getControlView() {
        return controlView;
    }

    /**
     * 获取暂停、播放View
     *
     * @return
     */
    public ImageView getPlayView() {
        return iv_ijk_play;
    }

    public View getProgressLayout() {
        return progress_layout;
    }

    /**
     * 获取当前时间View
     *
     * @return
     */
    public TextView getCurrentView() {
        return tv_ijk_current;
    }

    /**
     * 获取进度条View
     *
     * @return
     */
    public SeekBar getSeekBar() {
        return seek_ijk_bar;
    }

    public ProgressBar getProgressBar() {
        return progress_bar;
    }

    /**
     * 获取视频时长View
     *
     * @return
     */
    public TextView getDurationView() {
        return tv_ijk_duration;
    }

    /**
     * 获取屏幕转换视图
     *
     * @return
     */
    public ImageView getScreenSwitchView() {
        return iv_ijk_screen;
    }

    /**
     * 获取屏幕中间图片
     *
     * @return
     */
    /* public ImageView getCenterImageView() {
        return iv_ijk_center;
    } */

    /**
     * 获取音量、亮度控件
     *
     * @return
     */
    public IJKCircleProgressView getVoiceLightProgressView() {
        return ijk_center_progress;
    }

    /**
     * Loading视频
     *
     * @return
     */
    public FrameLayout getLoadingView() {
        return fl_ijk_loading;
    }

    /**
     * Loading ImageView
     *
     * @return
     */
    public ImageView getLoadingImageView() {
        return iv_ijk_loading;
    }

    /**
     * 封面控件
     *
     * @return
     */
    public ImageView getCoverImageView() {
        return iv_ijk_cover;
    }


    public ImageView getImageFfwdAnimationRipple() {
        return imageFfwdAnimationRipple;
    }

    public ImageView getImageRewindAnimationRipple() {
        return imageRewindAnimationRipple;
    }


    public ImageView getImagePlaybackAnimationRipple() {
        return imagePlaybackAnimationRipple;
    }

    public boolean controllerVisibility() {
        return progress_layout.getVisibility() == View.VISIBLE;
    }

    public void showController() {
        progress_layout.setVisibility(View.VISIBLE);
        progress_bar.setVisibility(View.GONE);
        if (fl_ijk_loading.getVisibility() != View.VISIBLE) {
            iv_ijk_play.setVisibility(View.VISIBLE);
        }
    }

    public void hideController() {
        progress_layout.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
        iv_ijk_play.setVisibility(View.GONE);
    }
}
