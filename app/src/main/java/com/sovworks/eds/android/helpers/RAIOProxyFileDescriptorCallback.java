package com.sovworks.eds.android.helpers;

import com.sovworks.eds.fs.RandomAccessIO;

import android.os.Build;
import android.os.ProxyFileDescriptorCallback;
import android.system.ErrnoException;
import android.system.OsConstants;

import androidx.annotation.RequiresApi;

import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RAIOProxyFileDescriptorCallback extends ProxyFileDescriptorCallback {

    private final RandomAccessIO mIO;

    public RAIOProxyFileDescriptorCallback(RandomAccessIO io) {
        mIO = io;
    }

    @Override
    public long onGetSize() throws ErrnoException {
        try {
            return mIO.length();
        } catch (IOException e) {
            throw new ErrnoException("onGetSize", OsConstants.EIO, e);
        }
    }

    @Override
    public int onRead(long offset, int size, byte[] data) throws ErrnoException {
        try {
            mIO.seek(offset);
            return mIO.read(data, 0, size);
        } catch (IOException e) {
            throw new ErrnoException("onRead", OsConstants.EIO, e);
        }
    }

    @Override
    public int onWrite(long offset, int size, byte[] data) throws ErrnoException {
        try {
            mIO.seek(offset);
            mIO.write(data, 0, size);
        } catch (IOException e) {
            throw new ErrnoException("onWrite", OsConstants.EIO, e);
        }
        return super.onWrite(offset, size, data);
    }

    @Override
    public void onFsync() throws ErrnoException {
        try {
            mIO.flush();
        } catch (IOException e) {
            throw new ErrnoException("onFsync", OsConstants.EIO, e);
        }
    }

    @Override
    public void onRelease() {
        try {
            mIO.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}