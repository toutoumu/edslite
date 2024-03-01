package androidx.ijk.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.ijk.view.IJKVideoView
import kotlin.math.abs

open class PlayerGestureHelper(
    var context: Context,
    var playerView: IJKVideoView,
    var appPreferences: AppPreferences,
    var listener: OnIjkVideoTouchListener,
    var audioManager: AudioManager,
) {
    /* lateinit var listener: OnIjkVideoTouchListener
    lateinit var context: Context
    lateinit var playerView: IJKVideoView
    lateinit var appPreferences: AppPreferences */

    /* constructor(context: Context, playerView: IJKVideoView, appPreferences: AppPreferences, listener: OnIjkVideoTouchListener) {
        this.context = context
        this.playerView = playerView
        this.appPreferences = appPreferences
        this.listener = listener
    } */


    init {
        @Suppress("ClickableViewAccessibility")
        playerView.setOnTouchListener { _, event ->
            if (playerView.useController) {
                when (event.pointerCount) {
                    1 -> {// 一个手指按下
                        tapGestureDetector.onTouchEvent(event)
                        if (appPreferences.playerGesturesVB) vbGestureDetector.onTouchEvent(event)
                        if (appPreferences.playerGesturesSeek) seekGestureDetector.onTouchEvent(event)
                    }

                    2 -> {
                        // if (appPreferences.playerGesturesZoom) zoomGestureDetector.onTouchEvent(event)
                    }
                }
            }
            releaseAction(event)
            true
        }
    }

    var isFullScreen = false

    /**
     * Tracks a value during a swipe gesture (between multiple onScroll calls).
     * When the gesture starts it's reset to an initial value and gets increased or decreased
     * (depending on the direction) as the gesture progresses.
     */

    private var swipeGestureValueTrackerVolume = -1f
    private var swipeGestureValueTrackerBrightness = -1f
    private var swipeGestureValueTrackerProgress = -1L

    private var swipeGestureVolumeOpen = false
    private var swipeGestureBrightnessOpen = false
    var swipeGestureProgressOpen = false

    private var lastScaleEvent: Long = 0

    /**
     * 单击双击事件
     */
    private var tapGestureDetector = GestureDetector(
        playerView.context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                /* playerView.apply {
                    if (!isControllerFullyVisible) showController() else hideController()
                } */

                // 显示或隐藏控件
                if (playerView.controlViewHolder.controllerVisibility()) {
                    playerView.controlViewHolder.hideController()
                } else {
                    playerView.controlViewHolder.showController()
                }
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
                val areaWidth = viewWidth / 5 // Divide the view into 5 parts: 2:1:2

                // Define the areas and their boundaries
                val leftmostAreaStart = 0
                val middleAreaStart = areaWidth * 2
                val rightmostAreaStart = middleAreaStart + areaWidth

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
        },
    )


    /**
     * 声音亮度调节
     */
    private val vbGestureDetector = GestureDetector(
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
                // Disables volume gestures when player is locked
                // if (isControlsLocked) return false

                if (abs(distanceY / distanceX) < 2) return false

                if (swipeGestureValueTrackerProgress > -1 || swipeGestureProgressOpen) {
                    return false
                }

                val viewCenterX = playerView.measuredWidth / 2

                // Distance to swipe to go from min to max
                val distanceFull = playerView.measuredHeight * Constants.FULL_SWIPE_RANGE_SCREEN_RATIO
                val ratioChange = distanceY / distanceFull

                if (firstEvent.x.toInt() > viewCenterX) {
                    // 声音 Swiping on the right, change volume
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (swipeGestureValueTrackerVolume == -1f) swipeGestureValueTrackerVolume = currentVolume.toFloat()

                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val change = ratioChange * maxVolume
                    swipeGestureValueTrackerVolume = (swipeGestureValueTrackerVolume + change).coerceIn(0f, maxVolume.toFloat())
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, swipeGestureValueTrackerVolume.toInt(), 0)


                    // 声音UI
                    // playerView.controlViewHolder.voiceLightProgressView.visibility = View.VISIBLE
                    // listener.onVideoChangeVoice(swipeGestureValueTrackerVolume, swipeGestureValueTrackerVolume / maxVolume.toFloat())

                    playerView.controlViewHolder.gestureVolumeLayout.visibility = View.VISIBLE
                    playerView.controlViewHolder.gestureVolumeProgressBar.max = maxVolume.times(100)
                    playerView.controlViewHolder.gestureVolumeProgressBar.progress = swipeGestureValueTrackerVolume.times(100).toInt()
                    val process = (swipeGestureValueTrackerVolume / maxVolume.toFloat()).times(100).toInt()
                    playerView.controlViewHolder.gestureVolumeText.text = "$process%"
                    playerView.controlViewHolder.gestureVolumeImage.setImageLevel(process)

                    swipeGestureVolumeOpen = true
                } else {
                    // 亮度 Swiping on the left, change brightness
                    val window = (context as Activity).window
                    val brightnessRange =
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF..WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL

                    // Initialize on first swipe
                    if (swipeGestureValueTrackerBrightness == -1f) {
                        val brightness = window.attributes.screenBrightness
                        // Timber.d("Brightness ${Settings.System.getFloat(activity.contentResolver, Settings.System.SCREEN_BRIGHTNESS)}")
                        swipeGestureValueTrackerBrightness = when (brightness) {
                            in brightnessRange -> brightness
                            else -> Settings.System.getFloat((context as Activity).contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255
                        }
                    }
                    swipeGestureValueTrackerBrightness = (swipeGestureValueTrackerBrightness + ratioChange).coerceIn(brightnessRange)
                    val lp = window.attributes
                    lp.screenBrightness = swipeGestureValueTrackerBrightness
                    window.attributes = lp

                    // 亮度UI
                    // playerView.controlViewHolder.voiceLightProgressView.visibility = View.VISIBLE
                    // listener.onVideoChangeBrightness(lp.screenBrightness, lp.screenBrightness)
                    // playerView.controlViewHolder.voiceLightProgressView.setMax(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL.times(100).toInt())
                    // playerView.controlViewHolder.voiceLightProgressView.setProgress(lp.screenBrightness.times(100).toInt())

                    playerView.controlViewHolder.gestureBrightnessLayout.visibility = View.VISIBLE
                    playerView.controlViewHolder.gestureBrightnessProgressBar.max =
                        WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL.times(100).toInt()
                    playerView.controlViewHolder.gestureBrightnessProgressBar.progress = lp.screenBrightness.times(100).toInt()
                    val process = (lp.screenBrightness / WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL).times(100).toInt()
                    playerView.controlViewHolder.gestureBrightnessText.text = "$process%"
                    playerView.controlViewHolder.gestureBrightnessImage.setImageLevel(process)

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

                // Check whether swipe was oriented vertically
                if (abs(distanceY / distanceX) < 2) {
                    // 如果没有加载出视频信息
                    if ((playerView.mediaPlayer?.duration ?: 0).coerceAtLeast(0) == 0L) {
                        swipeGestureProgressOpen = true
                        return true
                    }

                    return if (
                        (swipeGestureProgressOpen || abs(currentEvent.x - firstEvent.x) > 50) &&
                        !swipeGestureBrightnessOpen &&
                        !swipeGestureVolumeOpen &&
                        (SystemClock.elapsedRealtime() - lastScaleEvent) > 200
                    ) {
                        val currentPos = playerView.mediaPlayer?.currentPosition ?: 0
                        val vidDuration = (playerView.mediaPlayer?.duration ?: 0).coerceAtLeast(0)

                        val difference = ((currentEvent.x - firstEvent.x) * 90).toLong()
                        val newPos = (currentPos + difference).coerceIn(0, vidDuration)

                        /* playerView.controlViewHolder.voiceLightProgressView.visibility = View.VISIBLE
                        playerView.controlViewHolder.voiceLightProgressView.setProgress(newPos.toInt())
                        playerView.controlViewHolder.voiceLightProgressView.setMax(vidDuration.toInt())
                        playerView.controlViewHolder.voiceLightProgressView.setProgressText("进") */


                        // 更新底部当前时间 和进度
                        IJKVideoView.showVideoTime(newPos, playerView.controlViewHolder.currentView)
                        playerView.controlViewHolder.seekBar.progress = newPos.toInt()
                        playerView.controlViewHolder.progressBar.progress = newPos.toInt()

                        // 显示进度
                        playerView.controlViewHolder.playView.visibility = View.GONE
                        playerView.controlViewHolder.progressLayout.visibility = View.VISIBLE
                        playerView.controlViewHolder.progressBar.visibility = View.GONE
                        playerView.controlViewHolder.progressScrubberLayout.visibility = View.VISIBLE

                        playerView.controlViewHolder.progressScrubberText.text =
                            "${longToTimestamp(difference)}[${longToTimestamp(newPos, true)}]"
                        swipeGestureValueTrackerProgress = newPos
                        swipeGestureProgressOpen = true
                        true
                    } else {
                        false
                    }
                }
                return true
            }
        },
    )

    /**
     * 隐藏音量,亮度,进度提示
     */
    private fun releaseAction(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            // 音量
            // playerView.controlViewHolder.voiceLightProgressView.apply {
            playerView.controlViewHolder.gestureVolumeLayout.apply {
                if (visibility == View.VISIBLE) {
                    removeCallbacks(hideGestureVolumeIndicatorOverlayAction)
                    postDelayed(hideGestureVolumeIndicatorOverlayAction, 1000)
                    swipeGestureVolumeOpen = false
                }
            }
            // 亮度
            // playerView.controlViewHolder.voiceLightProgressView.apply {
            playerView.controlViewHolder.gestureBrightnessLayout.apply {
                if (visibility == View.VISIBLE) {
                    removeCallbacks(hideGestureBrightnessIndicatorOverlayAction)
                    postDelayed(hideGestureBrightnessIndicatorOverlayAction, 1000)
                    swipeGestureBrightnessOpen = false
                }
            }
            // 拖动进度
            // playerView.controlViewHolder.voiceLightProgressView.apply {
            playerView.controlViewHolder.progressScrubberLayout.apply {
                if (visibility == View.VISIBLE) {
                    if (swipeGestureValueTrackerProgress > -1) {
                        seekTo(swipeGestureValueTrackerProgress)
                    }
                    removeCallbacks(hideGestureProgressOverlayAction)
                    postDelayed(hideGestureProgressOverlayAction, 100)
                    swipeGestureProgressOpen = false

                    swipeGestureValueTrackerProgress = -1L
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
        // playerView.controlViewHolder.voiceLightProgressView.visibility = View.GONE
        playerView.controlViewHolder.gestureVolumeLayout.visibility = View.GONE
    }

    private val hideGestureBrightnessIndicatorOverlayAction = Runnable { // 亮度调节
        // playerView.controlViewHolder.voiceLightProgressView.visibility = View.GONE
        playerView.controlViewHolder.gestureBrightnessLayout.visibility = View.GONE
        /* if (appPreferences.playerBrightnessRemember) {
            appPreferences.playerBrightness = activity.window.attributes.screenBrightness
        } */
    }

    private val hideGestureProgressOverlayAction = Runnable { // 左右滑动进度
        // playerView.controlViewHolder.voiceLightProgressView.visibility = View.GONE
        playerView.controlViewHolder.progressScrubberLayout.visibility = View.GONE
    }

    val hideControllerAction = Runnable { // 底部进度栏
        playerView.controlViewHolder.progressLayout.visibility = View.GONE
        playerView.controlViewHolder.playView.visibility = View.GONE
        playerView.controlViewHolder.progressBar.visibility = View.VISIBLE
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
        playerView.mediaPlayer.seekTo(position)
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
     */
    fun longToTimestamp(duration: Long, noSign: Boolean = false): String {
        val sign = if (noSign) "" else if (duration < 0) "-" else "+"
        val seconds = abs(duration).div(1000)

        return String.format("%s%02d:%02d:%02d", sign, seconds / 3600, (seconds / 60) % 60, seconds % 60)
    }
}

object Constants {
    // player
    const val GESTURE_EXCLUSION_AREA_VERTICAL = 48
    const val GESTURE_EXCLUSION_AREA_HORIZONTAL = 24
    const val FULL_SWIPE_RANGE_SCREEN_RATIO = 1f// 0.66f
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