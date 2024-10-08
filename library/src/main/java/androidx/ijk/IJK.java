package androidx.ijk;

import android.view.View;

import androidx.annotation.LayoutRes;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Author: Relin
 * Describe:ijk配置
 * Date:2020/5/11 15:51
 */
public class IJK {

    /**
     * 显示方式 - 自适应
     */
    public static final int DISPLAY_WRAP_CONTENT = -1;
    /**
     * 显示方式 - 填充全屏
     */
    public static final int DISPLAY_MATCH_PARENT = 0;
    /**
     * 显示方式 - 填充截切
     */
    public static final int DISPLAY_MATCH_CROP = 1;

    /**
     * 显示方式 - 填充自适应
     */
    public static final int DISPLAY_MATCH_WRAP_CONTENT = 2;
    /**
     * 配置文件
     */
    private static IJK config;
    /**
     * 显示方式 (默认填充全屏)
     */
    private int displayType = DISPLAY_MATCH_PARENT;
    /**
     * 视频参数
     */
    private List<IJKOption> options;
    /**
     * 控制器视图
     */
    private View controlView;
    /**
     * 控制器视图ID
     */
    private int controlLayoutId = 0;

    /**
     * 是否自动播放
     */
    private boolean isAutoPlay = true;

    private IJK() {
        options = new ArrayList<>();
        // 加载IJK so文件
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取单例模式
     *
     * @return
     */
    public static IJK config() {
        if (config == null) {
            synchronized (IJK.class) {
                if (config == null) {
                    config = new IJK();
                }
            }
        }
        return config;
    }

    /**
     * 显示类型
     *
     * @return
     */
    public int displayType() {
        return displayType;
    }

    /**
     * 控制器视图
     *
     * @param controlView
     * @return
     */
    public IJK controlView(View controlView) {
        this.controlView = controlView;
        return this;
    }

    /**
     * 控制器视图
     *
     * @return
     */
    public View controlView() {
        return controlView;
    }

    /**
     * 控制器视图ID
     *
     * @param controlLayoutId
     * @return
     */
    public IJK controlLayoutId(@LayoutRes int controlLayoutId) {
        this.controlLayoutId = controlLayoutId;
        return this;
    }

    /**
     * 控制器视图ID
     *
     * @return
     */
    public int controlLayoutId() {
        return controlLayoutId;
    }

    /**
     * 是否自动播放
     *
     * @return
     */
    public boolean isAutoPlay() {
        return isAutoPlay;
    }

    /**
     * 是否自动播放
     *
     * @param autoPlay
     * @return
     */
    public IJK autoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
        return this;
    }

    /**
     * 参数列表
     *
     * @return
     */
    public List<IJKOption> options() {
        return options;
    }

    /**
     * 设置参数
     *
     * @param category 分类
     * @param name     名称
     * @param value    值
     * @return
     */
    public IJK option(int category, String name, String value) {
        IJKOption option = new IJKOption();
        option.setCategory(category);
        option.setName(name);
        option.setValue(value);
        options.add(option);
        return this;
    }

    /**
     * 设置参数
     *
     * @param category 分类
     * @param name     名称
     * @param value    值
     * @return
     */
    public IJK option(int category, String name, long value) {
        IJKOption option = new IJKOption();
        option.setCategory(category);
        option.setName(name);
        option.setValue(value);
        options.add(option);
        return this;
    }

    /**
     * 初始化参数
     *
     * @return
     */
    public IJK initOptions() {
        // 最大缓存
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max-buffer-size", 2 * 1024 * 1024);
        // 播放重连次数
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "reconnect", 5);
        // DNS重连
        option(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30L);
        option(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48L);
        // 开启硬解码
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_all_videos ", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg2 ", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        option(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec_mpeg4", 1);
        return this;
    }

    public IJK displayType(int displayType) {
        this.displayType = displayType;
        return this;
    }

}
