package com.sovworks.eds.android.filemanager.records;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.errors.UserException;
import com.sovworks.eds.android.filemanager.activities.rar.RarFileActivity;
import com.sovworks.eds.android.filemanager.activities.video.VideoActivity;
import com.sovworks.eds.android.helpers.CachedPathInfo;
import com.sovworks.eds.android.helpers.TempFilesMonitor;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.android.settings.UserSettings;
import com.sovworks.eds.exceptions.ApplicationException;
import com.sovworks.eds.fs.Path;
import com.sovworks.eds.fs.util.StringPathUtil;
import com.sovworks.eds.locations.Location;
import com.sovworks.eds.locations.Openable;
import com.sovworks.eds.settings.Settings;
import com.sovworks.eds.settings.SettingsCommon;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import timber.log.Timber;

public abstract class ExecutableFileRecordBase extends FileRecord {
    public ExecutableFileRecordBase(Context context) {
        super(context);
        _settings = UserSettings.getSettings(context);
    }

    @Override
    public void init(Location location, Path path) throws IOException {
        super.init(location, path);
        _loc = location;
    }

    @Override
    public boolean open() throws Exception {
        if (!isFile()) {
            return false;
        }
        String mime = FileOpsService.getMimeTypeFromExtension(_host, new StringPathUtil(getName()).getFileExtension());
        if (mime.startsWith("image/")) {
            openImageFile(_loc, this, false);
        } else if (
                mime.startsWith("video/") || mime.startsWith("audio/")
        ) {
            // 视频使用内置播放器打开
            Uri devUri = _loc.getDeviceAccessibleUri(this.getPath());
            Intent intent = new Intent();
            intent.putExtra("uri", devUri);
            intent.setClass(_host.getBaseContext(), VideoActivity.class);
            _host.startActivity(intent);
        } else if (mime.startsWith("application/x-rar-compressed")) {
            // 压缩文件处理
            Timber.e("点击压缩文件");
            Uri devUri = _loc.getDeviceAccessibleUri(this.getPath());
            Intent intent = new Intent();
            intent.putExtra("uri", devUri);
            intent.setClass(_host.getBaseContext(), RarFileActivity.class);
            _host.startActivity(intent);
        } else {
            startDefaultFileViewer(_loc, this);
        }
        return true;
    }

    @Override
    public boolean openInplace() throws Exception {
        if (!isFile()) {
            return false;
        }

        String mime = FileOpsService.getMimeTypeFromExtension(_host, new StringPathUtil(getName()).getFileExtension());
        if (mime.startsWith("image/")) {
            openImageFile(_loc, this, true);
            return true;
        }
        _host.showProperties(this, true);
        return open();
    }

    protected Location _loc;
    protected final Settings _settings;

    protected void extractFileAndStartViewer(Location location, BrowserRecord rec) throws UserException, IOException {
        if (rec.getSize() > 1024 * 1024 * _settings.getMaxTempFileSize()) {
            throw new UserException(_host, R.string.err_temp_file_is_too_big);
        }
        Location loc = location.copy();
        loc.setCurrentPath(rec.getPath());
        TempFilesMonitor.getMonitor(_context).startFile(loc);
    }

    protected void startDefaultFileViewer(Location location, BrowserRecord rec) throws IOException, UserException, ApplicationException {
        Uri devUri = location.getDeviceAccessibleUri(rec.getPath());
        if (devUri != null) {
            FileOpsService.startFileViewer(_host, devUri, FileOpsService.getMimeTypeFromExtension(_context, new StringPathUtil(rec.getName()).getFileExtension()));
        } else {
            extractFileAndStartViewer(location, rec);
        }
    }

    protected void openImageFile(Location location, BrowserRecord rec, boolean inplace) throws IOException, UserException, ApplicationException {
        /*int ivMode = _settings.getInternalImageViewerMode();
        if (ivMode == SettingsCommon.USE_INTERNAL_IMAGE_VIEWER_ALWAYS ||
                (ivMode == SettingsCommon.USE_INTERNAL_IMAGE_VIEWER_VIRT_FS &&
                        location instanceof Openable)
        ) {
            _host.showPhoto(rec, inplace);
        } else {
            if (inplace) {
                _host.showPhoto(rec, true);
            }
            startDefaultFileViewer(location, rec);
        }*/

        // 获取目录下所有文件
        NavigableSet<CachedPathInfo> files = (NavigableSet<CachedPathInfo>) _host.getCurrentFiles();
        if (files.isEmpty()) {
            return;
        }

        // 获取所有图片
        List<CachedPathInfo> images = new ArrayList<>();
        for (CachedPathInfo file : files) {
            String mime1 = FileOpsService.getMimeTypeFromExtension(_host.getBaseContext(), new StringPathUtil(file.getName()).getFileExtension());
            // Timber.e(file.getName() + " : " + mime1);
            if (mime1.startsWith("image/")) {
                images.add(file);
            }
        }
        if (images.isEmpty()) {
            return;
        }

        int index = images.indexOf(this);
        new StfalconImageViewer.Builder<>(_host.getBaseContext(), images, (imageView, image) ->
                Glide.with(_host.getBaseContext())
                        .load(image.getPath())
                        .into(imageView))
                .withStartPosition(index)
                .show(true);
    }
}