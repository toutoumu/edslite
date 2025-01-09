package com.sovworks.eds.android.helpers;


import android.media.MediaDataSource;

import com.sovworks.eds.fs.RandomAccessIO;

import java.io.IOException;

public class EdsMediaMetadataSource extends MediaDataSource {

    private final RandomAccessIO mIO;

    public EdsMediaMetadataSource(RandomAccessIO io) {
        mIO = io;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        mIO.seek(position);
        return mIO.read(buffer, offset, size);
    }

    @Override
    public long getSize() throws IOException {
        return mIO.length();
    }

    @Override
    public void close() throws IOException {
        mIO.close();
    }
}