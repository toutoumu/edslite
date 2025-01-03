package androidx.ijk.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
import android.view.WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.ijk.view.IJKVideoView
import cn.yinxm.media.video.gesture.touch.adapter.IVideoTouchAdapter
import cn.yinxm.media.video.gesture.touch.handler.VideoTouchScaleHandler
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import kotlin.math.abs
import kotlin.math.log

@SuppressLint("ClickableViewAccessibility")
open class PlayerGestureHelper(
    var context: Context,
    var playerView: IJKVideoView,
    var appPreferences: AppPreferences,
    var audioManager: AudioManager,
) {
    private val TAG = "PlayerGestureHelper"

    init {
        @Suppress("ClickableViewAccessibility")
        playerView.setOnTouchListener { _, event ->
            if (playerView.useController) {
                when (event.pointerCount) {
                    1 -> { // 一个手指按下
                        tapGestureDetector.onTouchEvent(event)
                        if (appPreferences.playerGesturesVB) vbGestureDetector.onTouchEvent(event)
                        if (appPreferences.playerGesturesSeek) seekGestureDetector.onTouchEvent(event)

                        // 恢复缩放按钮显示逻辑
                        if (event.action == MotionEvent.ACTION_UP) {
                            if (mScaleHandler.isScaled) {
                                mScaleHandler.showScaleReset()
                            }
                        }
                    }

                    2 -> {
                        // if (appPreferences.playerGesturesZoom) zoomGestureDetector.onTouchEvent(event)
                    }
                }
                if (!swipeGestureProgressOpen && !swipeGestureBrightnessOpen && !swipeGestureVolumeOpen) {
                    zoomGestureDetector.onTouchEvent(event)
                }
            }
            releaseAction(event)
            true
        }
    }

    var isFullScreen = false

    // 声音调整是否进行中
    private var swipeGestureVolumeOpen = false

    // 亮度调整是否进行中
    private var swipeGestureBrightnessOpen = false

    // 播放进度调整是否进行中
    var swipeGestureProgressOpen = false

    // 当前进度
    private var currentProgress = -1L

    private var lastScaleEvent: Long = 0

    /**
     * 单击双击事件, 或拖动事件
     */
    private var tapGestureDetector = GestureDetector(
        playerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // 显示或隐藏控件
                if (playerView.controlViewHolder.controllerVisibility()) {
                    playerView.controlViewHolder.hideController()
                } else {
                    playerView.controlViewHolder.showController()
                }
                // 5000ms 后隐藏控件
                playerView.controlViewHolder.progressLayout.apply {
                    if (visibility == View.VISIBLE) {
                        removeCallbacks(hideControllerAction)
                        postDelayed(hideControllerAction, 5000)
                    }
                }
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Disables double tap gestures if view is locked
                // if (isControlsLocked) return false

                val viewWidth = playerView.measuredWidth
                val areaWidth = viewWidth / 5 // Divide the view into 5 parts: 1:3:1

                // Define the areas and their boundaries
                val leftmostAreaStart = 0
                val middleAreaStart = areaWidth * 1
                val rightmostAreaStart = areaWidth * 4

                when (e.x.toInt()) {
                    in leftmostAreaStart until middleAreaStart -> {
                        // Tapped on the leftmost area (seek backward)
                        rewind()
                    }

                    in middleAreaStart until rightmostAreaStart -> {
                        // Tapped on the middle area (toggle pause/unpause)
                        togglePlayback()
                    }

                    in rightmostAreaStart until viewWidth -> {
                        // Tapped on the rightmost area (seek forward)
                        fastForward()
                    }
                }
                return true
            }

            override fun onScroll(
                firstEvent: MotionEvent,
                currentEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (firstEvent == null) {
                    return false
                }
                // 缩放状态, 单指滑动
                if (mScaleHandler.isInScaleStatus) {
                    return mScaleHandler.onScroll(distanceX, distanceY)
                }
                return false
            }
        },
    )


    /**
     * 声音亮度调节
     */
    private val vbGestureDetector = GestureDetector(
        playerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            private var currentVolume = -1f
            private var currentBrightness = -1f

            @SuppressLint("SetTextI18n")
            override fun onScroll(
                firstEvent: MotionEvent,
                currentEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                // Excludes area where app gestures conflicting with system gestures
                // if (inExclusionArea(firstEvent)) return false
                // Disables volume gestures when player is locked
                // if (isControlsLocked) return false

                if (firstEvent == null) {
                    return false
                }

                if (abs(distanceY / distanceX) < 2) return false

                // 正在进行进度调整, 视频在缩放状态
                if (currentProgress > -1 || swipeGestureProgressOpen || mScaleHandler.isInScaleStatus) {
                    return false
                }

                val viewCenterX = playerView.measuredWidth / 2

                // Distance to swipe to go from min to max
                val distanceFull = playerView.measuredHeight * Constants.FULL_SWIPE_RANGE_SCREEN_RATIO
                val ratioChange = distanceY / distanceFull

                if (firstEvent.x.toInt() > viewCenterX) {
                    // 声音 Swiping on the right, change volume
                    if (currentVolume == -1f) {
                        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                        this.currentVolume = currentVolume.toFloat()
                    }

                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val change = ratioChange * maxVolume
                    currentVolume = (currentVolume + change).coerceIn(0f, maxVolume.toFloat())
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume.toInt(), 0)

                    // 声音UI
                    playerView.controlViewHolder.apply {
                        gestureVolumeLayout.visibility = View.VISIBLE
                        gestureVolumeProgressBar.max = maxVolume.times(100)
                        gestureVolumeProgressBar.progress = currentVolume.times(100).toInt()
                        val process = (currentVolume / maxVolume.toFloat()).times(100).toInt()
                        gestureVolumeText.text = "$process%"
                        gestureVolumeImage.setImageLevel(process)
                    }
                    swipeGestureVolumeOpen = true
                } else {
                    // 亮度 Swiping on the left, change brightness
                    val window = (context as Activity).window
                    val brightnessRange = BRIGHTNESS_OVERRIDE_OFF..BRIGHTNESS_OVERRIDE_FULL

                    // Initialize on first swipe
                    if (currentBrightness == -1f) {
                        val brightness = window.attributes.screenBrightness
                        currentBrightness = when (brightness) {
                            in brightnessRange -> brightness
                            else -> Settings.System.getFloat(context.contentResolver, SCREEN_BRIGHTNESS) / 255
                        }
                    }
                    currentBrightness = (currentBrightness + ratioChange).coerceIn(brightnessRange)
                    val layoutParams = window.attributes
                    layoutParams.screenBrightness = currentBrightness
                    window.attributes = layoutParams

                    // 亮度UI
                    playerView.controlViewHolder.apply {
                        gestureBrightnessLayout.visibility = View.VISIBLE
                        gestureBrightnessProgressBar.max = BRIGHTNESS_OVERRIDE_FULL.times(100).toInt()
                        gestureBrightnessProgressBar.progress = layoutParams.screenBrightness.times(100).toInt()
                        val process = (layoutParams.screenBrightness / BRIGHTNESS_OVERRIDE_FULL).times(100).toInt()
                        gestureBrightnessText.text = "$process%"
                        gestureBrightnessImage.setImageLevel(process)
                    }
                    swipeGestureBrightnessOpen = true
                }
                return true
            }
        },
    )

    /**
     * 快进快退
     */
    private val seekGestureDetector = GestureDetector(
        playerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            @SuppressLint("SetTextI18n")
            override fun onScroll(
                firstEvent: MotionEvent,
                currentEvent: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                // Excludes area where app gestures conflicting with system gestures
                // if (inExclusionArea(firstEvent)) return false
                // Disables seek gestures if view is locked
                // if (isControlsLocked) return false

                if (firstEvent == null) {
                    return false
                }
                // Check whether swipe was oriented vertically
                if (abs(distanceY / distanceX) > 2) return false

                // 如果没有加载出视频信息
                if ((playerView.mediaPlayer?.duration ?: 0).coerceAtLeast(0) == 0L) {
                    swipeGestureProgressOpen = true
                    return true
                }

                if (
                    (swipeGestureProgressOpen || abs(currentEvent.x - firstEvent.x) > 50) &&
                    !swipeGestureBrightnessOpen &&
                    !swipeGestureVolumeOpen &&
                    !mScaleHandler.isInScaleStatus &&
                    (SystemClock.elapsedRealtime() - lastScaleEvent) > 200
                ) {
                    val currentPos = playerView.mediaPlayer?.currentPosition ?: 0
                    val vidDuration = (playerView.mediaPlayer?.duration ?: 0).coerceAtLeast(0)

                    val difference = ((currentEvent.x - firstEvent.x) * 90).toLong()
                    val newPos = (currentPos + difference).coerceIn(0, vidDuration)
                    currentProgress = newPos
                    swipeGestureProgressOpen = true

                    playerView.controlViewHolder.apply {
                        // 更新底部当前时间 和进度
                        IJKVideoView.showVideoTime(newPos, currentView)
                        seekBar.progress = newPos.toInt()
                        smallProgressBar.progress = newPos.toInt()

                        // 显示进度
                        playView.visibility = View.GONE
                        progressLayout.visibility = View.VISIBLE
                        smallProgressBar.visibility = View.GONE
                        progressScrubberLayout.visibility = View.VISIBLE
                        progressScrubberText.text =
                            "${longToTimestamp(difference)}[${longToTimestamp(newPos, true)}]"
                    }
                    return true
                }
                return false
            }
        },
    )

    /**
     * 缩放手势
     */
    private var zoomGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return mScaleHandler.onScaleBegin(detector)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return mScaleHandler.onScale(detector)
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                mScaleHandler.onScaleEnd(detector)
            }
        })

    // 缩放处理
    var mScaleHandler = VideoTouchScaleHandler(playerView.context, playerView, object : IVideoTouchAdapter {
        override fun getTextureView(): TextureView {
            return playerView.textureView
        }

        override fun getMediaPlayer(): IjkMediaPlayer {
            return playerView.mediaPlayer
        }

        override fun isPlaying(): Boolean {
            return playerView.isPlaying
        }
    })

    /**
     * 隐藏音量,亮度,进度提示
     */
    private fun releaseAction(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            // 缩放
            if (mScaleHandler.isScrolling) {
                mScaleHandler.onScrollEnd()
            }
            // 音量
            playerView.controlViewHolder.gestureVolumeLayout.apply {
                if (visibility == View.VISIBLE) {
                    removeCallbacks(hideGestureVolumeIndicatorOverlayAction)
                    postDelayed(hideGestureVolumeIndicatorOverlayAction, 1000)
                    swipeGestureVolumeOpen = false
                }
            }
            // 亮度
            playerView.controlViewHolder.gestureBrightnessLayout.apply {
                if (visibility == View.VISIBLE) {
                    removeCallbacks(hideGestureBrightnessIndicatorOverlayAction)
                    postDelayed(hideGestureBrightnessIndicatorOverlayAction, 1000)
                    swipeGestureBrightnessOpen = false
                }
            }
            // 拖动进度
            playerView.controlViewHolder.progressScrubberLayout.apply {
                if (visibility == View.VISIBLE) {
                    if (currentProgress > -1) {
                        seekTo(currentProgress)
                    }
                    removeCallbacks(hideGestureProgressOverlayAction)
                    postDelayed(hideGestureProgressOverlayAction, 100)
                    swipeGestureProgressOpen = false

                    currentProgress = -1L
                }
            }
            // 底部进度
            playerView.controlViewHolder.progressLayout.apply {
                if (visibility == View.VISIBLE) {
                    removeCallbacks(hideControllerAction)
                    postDelayed(hideControllerAction, 5000)
                }
            }
        }
    }

    private val hideGestureVolumeIndicatorOverlayAction = Runnable { // 音量调节
        playerView.controlViewHolder.gestureVolumeLayout.visibility = View.GONE
    }

    private val hideGestureBrightnessIndicatorOverlayAction = Runnable { // 亮度调节
        playerView.controlViewHolder.gestureBrightnessLayout.visibility = View.GONE
        /* if (appPreferences.playerBrightnessRemember) {
            appPreferences.playerBrightness = activity.window.attributes.screenBrightness
        } */
    }

    private val hideGestureProgressOverlayAction = Runnable { // 左右滑动进度
        playerView.controlViewHolder.progressScrubberLayout.visibility = View.GONE
    }

    val hideControllerAction = Runnable { // 底部进度栏
        playerView.controlViewHolder.progressLayout.visibility = View.GONE
        playerView.controlViewHolder.playView.visibility = View.GONE
        playerView.controlViewHolder.smallProgressBar.visibility = View.VISIBLE
    }

    /**
     * 前进
     */
    private fun fastForward() {
        val currentPosition = playerView.mediaPlayer?.currentPosition ?: 0
        val fastForwardPosition = currentPosition + appPreferences.playerSeekForwardIncrement
        seekTo(fastForwardPosition)
        animateRipple(playerView.controlViewHolder.imageFfwdAnimationRipple)
    }

    /**
     * 后退
     */
    private fun rewind() {
        val currentPosition = playerView.mediaPlayer?.currentPosition ?: 0
        val rewindPosition = currentPosition - appPreferences.playerSeekBackIncrement
        seekTo(rewindPosition.coerceAtLeast(0))
        animateRipple(playerView.controlViewHolder.imageRewindAnimationRipple)
    }

    /**
     * 暂停 播放
     */
    private fun togglePlayback() {
        // playerView.mediaPlayer?.playWhenReady = !playerView.mediaPlayer?.playWhenReady!!
        if (playerView.mediaPlayer?.isPlayable == true) {
            if (playerView.isPlayEnd) {
                playerView.restart()
            } else {
                if (playerView.mediaPlayer != null && playerView.isPlaying) {
                    playerView.pause()
                } else {
                    if (playerView.isPrepared) {
                        playerView.resume()
                    } else {
                        playerView.start()
                    }
                }
            }
            animateRipple(playerView.controlViewHolder.imagePlaybackAnimationRipple)
        }
    }

    /**
     * 跳转到指定为止
     */
    private fun seekTo(position: Long) {
        val vidDuration = (playerView.mediaPlayer?.duration ?: 0).coerceAtLeast(0)
        playerView.mediaPlayer?.seekTo(position.coerceIn(0, vidDuration))
    }

    /**
     * 动画
     */
    private fun animateRipple(image: ImageView) {
        image
            .animateSeekingRippleStart()
            .withEndAction {
                resetRippleImage(image)
            }
            .start()
    }

    private fun ImageView.animateSeekingRippleStart(): ViewPropertyAnimator {
        val rippleImageHeight = this.height
        val playerViewHeight = playerView.height.toFloat()
        val playerViewWidth = playerView.width.toFloat()
        val scaleDifference = playerViewHeight / rippleImageHeight
        val playerViewAspectRatio = playerViewWidth / playerViewHeight
        val scaleValue = scaleDifference * playerViewAspectRatio
        return animate()
            .alpha(1f)
            .scaleX(scaleValue)
            .scaleY(scaleValue)
            .setDuration(180)
            .setInterpolator(DecelerateInterpolator())
    }

    private fun resetRippleImage(image: ImageView) {
        image
            .animateSeekingRippleEnd()
            .withEndAction {
                image.scaleX = 1f
                image.scaleY = 1f
            }
            .start()
    }

    private fun ImageView.animateSeekingRippleEnd() = animate()
        .alpha(0f)
        .setDuration(150)
        .setInterpolator(AccelerateInterpolator())

    /**
     * 时间格式
     * @param duration 毫秒
     * @param noSign 是否不带符号
     */
    @SuppressLint("DefaultLocale")
    fun longToTimestamp(duration: Long, noSign: Boolean = false): String {
        val sign = if (noSign) "" else if (duration < 0) "-" else "+"
        val seconds = abs(duration).div(1000)

        return String.format(
            "%s%02d:%02d:%02d",
            sign,
            seconds / 3600,
            (seconds / 60) % 60,
            seconds % 60
        )
    }
}

object Constants {
    // player
    const val GESTURE_EXCLUSION_AREA_VERTICAL = 48
    const val GESTURE_EXCLUSION_AREA_HORIZONTAL = 24
    const val FULL_SWIPE_RANGE_SCREEN_RATIO = 1f // 0.66f
    const val ZOOM_SCALE_BASE = 1f
    const val ZOOM_SCALE_THRESHOLD = 0.01f

    // pref
    const val PREF_CURRENT_SERVER = "pref_current_server"
    const val PREF_PLAYER_PREFERRED_QUALITY = "pref_player_preferred_quality"
    const val PREF_OFFLINE_MODE = "pref_offline_mode"
    const val PREF_PLAYER_GESTURES = "pref_player_gestures"
    const val PREF_PLAYER_GESTURES_VB = "pref_player_gestures_vb"
    const val PREF_PLAYER_GESTURES_ZOOM = "pref_player_gestures_zoom"
    const val PREF_PLAYER_GESTURES_SEEK = "pref_player_gestures_seek"
    const val PREF_PLAYER_BRIGHTNESS_REMEMBER = "pref_player_brightness_remember"
    const val PREF_PLAYER_BRIGHTNESS = "pref_player_brightness"
    const val PREF_PLAYER_SEEK_BACK_INC = "pref_player_seek_back_inc"
    const val PREF_PLAYER_SEEK_FORWARD_INC = "pref_player_seek_forward_inc"
    const val PREF_PLAYER_MPV = "pref_player_mpv"
    const val PREF_PLAYER_MPV_HWDEC = "pref_player_mpv_hwdec"
    const val PREF_PLAYER_MPV_HWDEC_CODECS = "pref_player_mpv_hwdec_codecs"
    const val PREF_PLAYER_MPV_VO = "pref_player_mpv_vo"
    const val PREF_PLAYER_MPV_AO = "pref_player_mpv_ao"
    const val PREF_PLAYER_MPV_GPU_API = "pref_player_mpv_gpu_api"
    const val PREF_PLAYER_INTRO_SKIPPER = "pref_player_intro_skipper"
    const val PREF_PLAYER_TRICK_PLAY = "pref_player_trick_play"
    const val PREF_AUDIO_LANGUAGE = "pref_audio_language"
    const val PREF_SUBTITLE_LANGUAGE = "pref_subtitle_language"
    const val PREF_IMAGE_CACHE = "pref_image_cache"
    const val PREF_IMAGE_CACHE_SIZE = "pref_image_cache_size"
    const val PREF_THEME = "theme"
    const val PREF_DYNAMIC_COLORS = "dynamic_colors"
    const val PREF_AMOLED_THEME = "pref_amoled_theme"
    const val PREF_SPAN_NO = "pref_span_count"
    const val PREF_NETWORK_REQUEST_TIMEOUT = "pref_network_request_timeout"
    const val PREF_NETWORK_CONNECT_TIMEOUT = "pref_network_connect_timeout"
    const val PREF_NETWORK_SOCKET_TIMEOUT = "pref_network_socket_timeout"
    const val PREF_DOWNLOADS_MOBILE_DATA = "pref_downloads_mobile_data"
    const val PREF_DOWNLOADS_ROAMING = "pref_downloads_roaming"
    const val PREF_SORT_BY = "pref_sort_by"
    const val PREF_SORT_ORDER = "pref_sort_order"
    const val PREF_DISPLAY_EXTRA_INFO = "pref_display_extra_info"

    // caching
    const val DEFAULT_CACHE_SIZE = 20

    // favorites
    const val FAVORITE_TYPE_MOVIES = 0
    const val FAVORITE_TYPE_SHOWS = 1
    const val FAVORITE_TYPE_EPISODES = 2

    // network
    const val NETWORK_DEFAULT_REQUEST_TIMEOUT = 30_000L
    const val NETWORK_DEFAULT_CONNECT_TIMEOUT = 6_000L
    const val NETWORK_DEFAULT_SOCKET_TIMEOUT = 10_000L

    // sorting
    // This values must correspond to a SortString from [SortBy]
    const val DEFAULT_SORT_BY = "SortName"
    const val DEFAULT_SORT_ORDER = "Ascending"
}