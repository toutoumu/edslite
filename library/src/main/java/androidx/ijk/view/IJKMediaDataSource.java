package androidx.ijk.view;

import java.io.IOException;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;


@SuppressWarnings("RedundantThrows")
public interface IJKMediaDataSource extends IMediaDataSource {
    /**
     * 返回当前数据源的新实例, 用于重新播放视频
     *
     * @return IMediaDataSource
     * @throws IOException .
     */
    IJKMediaDataSource newDataSource() throws IOException;
}
