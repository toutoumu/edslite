package com.sovworks.eds.android;

import android.content.Context;

import androidx.ijk.IJK;

public class EdsApplication extends EdsApplicationBase {
    @Override
    public void onCreate() {
        super.onCreate();
        initIJK();
    }

    public static void stopProgram(Context context, boolean exitProcess) {
        stopProgramBase(context, exitProcess);
        if (exitProcess) {
            exitProcess();
        }
    }

    /**
     * 初始化ijk播放器
     */
    private static void initIJK() {
        // 初始化建议配置在Application
        IJK ijk = IJK.config();
        ijk.displayType(IJK.DISPLAY_MATCH_WRAP_CONTENT);
        // 启用硬解码器
        /* ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        // 启用硬解码器（如果设备支持）
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "videotoolbox", 1);
        // 设置探测缓冲区大小
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10);
        // 设置最小缓冲帧数
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 100);
        // 设置最大缓冲区大小（默认是0，表示无限制）
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 0);
        // 设置最小缓冲帧数
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 5);
        // 设置超时时间
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000);
        // 设置启动时的探测时间
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 100L);
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzemaxduration", 100L);
        // 关闭播放缓冲区
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0L);
        // 立刻写出处理完的Packet
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "flush_packets", 1L);
        // 允许丢帧
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1L);
        // 调用prepareAsync()方法后是否自动开始播放
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1L);
        // 优化进度跳转
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        ijk.option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek"); */
    }
}
