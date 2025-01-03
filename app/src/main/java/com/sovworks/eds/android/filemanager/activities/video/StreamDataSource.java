package com.sovworks.eds.android.filemanager.activities.video;


import static com.sovworks.eds.android.providers.MainContentProviderBase.getLocationFromProviderUri;

import android.content.Context;
import android.net.Uri;

import androidx.ijk.view.IJKMediaDataSource;

import com.sovworks.eds.fs.File;
import com.sovworks.eds.fs.RandomAccessIO;
import com.sovworks.eds.fs.util.Util;
import com.sovworks.eds.locations.Location;

import java.io.FileNotFoundException;
import java.io.IOException;

public class StreamDataSource implements IJKMediaDataSource {
    private final Uri uri;
    private final Context context;
    private long mPosition = 0;

    private RandomAccessIO stream;

    File f;

    public StreamDataSource(Context context, Uri uri) throws IOException {
        this.uri = uri;
        this.context = context;
        Location loc = getLocationFromProviderUri(context, uri);
        File.AccessMode am = Util.getAccessModeFromString("r");
        if (!loc.getCurrentPath().isFile() && am == File.AccessMode.Read) {
            throw new FileNotFoundException();
        }
        f = loc.getCurrentPath().getFile();

        stream = f.getRandomAccessIO(am);
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (size <= 0) {
            // Logger.debug("未读取,position = " + size + " size = " + size + " offset = " + offset);
            return size;
        }
        // Logger.debug("读取开始");
        if (mPosition != position) {
            stream.seek(position);
        }
        int length = stream.read(buffer, offset, size);
        // Logger.debug("读取数据长度: " + length);
        mPosition += length;
        return length;
    }

    @Override
    public long getSize() throws IOException {
        // return stream.available();
        return f.getSize();
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            // Logger.debug("读取结束,关闭");
            stream.close();
        }
        stream = null;
    }

    @Override
    public IJKMediaDataSource newDataSource() throws IOException {
        return new StreamDataSource(context, uri);
    }
}
