package com.sovworks.eds.android.filemanager.records;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.sovworks.eds.android.R;
import com.sovworks.eds.android.errors.UserException;
import com.sovworks.eds.android.filemanager.activities.rar.RarFileActivity;
import com.sovworks.eds.android.filemanager.activities.video.VideoActivity;
import com.sovworks.eds.android.filemanager.activities.zip.ZipFileActivity;
import com.sovworks.eds.android.helpers.CachedPathInfo;
import com.sovworks.eds.android.helpers.TempFilesMonitor;
import com.sovworks.eds.android.providers.MainContentProvider;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.android.settings.UserSettings;
import com.sovworks.eds.exceptions.ApplicationException;
import com.sovworks.eds.fs.Path;
import com.sovworks.eds.fs.util.StringPathUtil;
import com.sovworks.eds.locations.Location;
import com.sovworks.eds.locations.Openable;
import com.sovworks.eds.settings.Settings;
import com.sovworks.eds.settings.SettingsCommon;

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
        final String fileExtension = new StringPathUtil(getName()).getFileExtension();
        final String mime = FileOpsService.getMimeTypeFromExtension(_host.getBaseContext(), fileExtension);
        if (mime.startsWith("image/")) {
            Timber.e("点击图片: %s %s", _loc.getTitle(), this.getPath());
            openImageFile(_loc, this);
        } else if (mime.startsWith("video/") || mime.startsWith("audio/")) {
            Timber.e("点击视频: %s %s", _loc.getTitle(), this.getPath());
            openIJK();
            // openMPV();
        }
        // zip 文件交给系统处理
        // else if (mime.startsWith("application/zip")) {
        //     Timber.e("点击ZIP文件: %s %s", _loc.getTitle(), this.getPath());
        //     openZIP();
        // }
        // RAR文件
        else if (mime.startsWith("application/x-rar-compressed")) {
            Timber.e("点击RAR文件: %s %s", _loc.getTitle(), this.getPath());
            openRAR();
        } else {
            // 暂时不支持的文件
            Timber.e("点击其他类型文件: %s %s", _loc.getTitle(), this.getPath());
            startDefaultFileViewer(_loc, this);
        }
        return true;
    }


    @Override
    public boolean openInplace() throws Exception {
        if (!isFile()) {
            return false;
        }
        final String fileExtension = new StringPathUtil(getName()).getFileExtension();
        final String mime = FileOpsService.getMimeTypeFromExtension(_host.getBaseContext(), fileExtension);
        if (mime.startsWith("image/")) {
            openImageFile(_loc, this, true);
            return true;
        }
        _host.showProperties(this, true);
        return open();
    }

    protected Location _loc;
    protected final Settings _settings;

    /**
     * 解压文件并显示
     *
     * @param location
     * @param rec
     * @throws UserException
     * @throws IOException
     */
    protected void extractFileAndStartViewer(Location location, BrowserRecord rec) throws UserException, IOException {
        if (rec.getSize() > 1024L * 1024 * _settings.getMaxTempFileSize()) {
            throw new UserException(_host, R.string.err_temp_file_is_too_big);
        }
        Location loc = location.copy();
        loc.setCurrentPath(rec.getPath());
        TempFilesMonitor.getMonitor(_context).startFile(loc);
    }

    /**
     * 使用系统自带的文件查看器打开文件
     *
     * @param location 容器
     * @param rec
     * @throws IOException
     * @throws UserException
     * @throws ApplicationException
     */
    protected void startDefaultFileViewer(Location location, BrowserRecord rec) throws IOException, UserException, ApplicationException {
        Uri devUri = location.getDeviceAccessibleUri(rec.getPath());
        if (devUri != null) {
            Timber.e("使用系统自带的文件查看器打开文件: %s", location.getClass());
            FileOpsService.startFileViewer(_host, devUri, FileOpsService.getMimeTypeFromExtension(_context, new StringPathUtil(rec.getName()).getFileExtension()));
        } else {
            Timber.e("解压文件并使用系统自带的文件查看器打开文件: %s", location.getClass());
            extractFileAndStartViewer(location, rec);
        }
    }

    /**
     * 竖屏时,打开图片浏览器
     *
     * @param location 容器
     * @param rec      当前文件( rec === this)
     */
    private void openImageFile(Location location, BrowserRecord rec) {
        // 获取目录下所有文件
        NavigableSet<CachedPathInfo> files = (NavigableSet<CachedPathInfo>) _host.getCurrentFiles();
        if (files.isEmpty()) {
            return;
        }
        // 获取所有图片
        List<CachedPathInfo> images = new ArrayList<>();
        List<CachedPathInfo> list = new ArrayList<>();
        for (CachedPathInfo file : files) {
            final String fileExtension = new StringPathUtil(file.getName()).getFileExtension();
            final String mime = FileOpsService.getMimeTypeFromExtension(_host.getBaseContext(), fileExtension);
            list.add(file);
            if (mime.startsWith("image/")) {
                images.add(file);
            }
        }
        if (images.isEmpty()) {
            return;
        }

        int index = images.indexOf(this);
        ListView listView = _host.getFileListViewFragment().getListView();
        // 找到点击的图片
        ImageView firstImage = null;
        {
            int realPosition = list.indexOf(this);
            int firstVisiblePosition = listView.getFirstVisiblePosition();
            int childIndex = realPosition - firstVisiblePosition;
            if (childIndex >= 0 && childIndex < listView.getChildCount()) {
                listView.smoothScrollToPosition(realPosition);
                View itemView = listView.getChildAt(childIndex);
                firstImage = itemView.findViewById(android.R.id.icon);
            }
        }

        _host.getFileListViewFragment().showView(list, images, firstImage, index);

        /*int index = images.indexOf(this);
        new StfalconImageViewer.Builder<>(_host, images, (imageView, image) ->
                Glide.with(_host)
                        .load(image.getPath())
                        .into(imageView))
                .withStartPosition(index)
                .withHiddenStatusBar(false)
                .show(true);*/
    }

    /**
     * 打开图片文件
     *
     * @param location 当前目录
     * @param rec      当前文件
     * @param inplace  是否是横屏模式
     */
    protected void openImageFile(Location location, BrowserRecord rec, boolean inplace) throws IOException, UserException, ApplicationException {
        int ivMode = _settings.getInternalImageViewerMode();
        if (ivMode == SettingsCommon.USE_INTERNAL_IMAGE_VIEWER_ALWAYS || (ivMode == SettingsCommon.USE_INTERNAL_IMAGE_VIEWER_VIRT_FS && location instanceof Openable)) {
            _host.showPhoto(rec, inplace);
            Timber.e("使用内部查看器查看图片: %s", inplace);
        } else {
            if (inplace) {
                startDefaultFileViewer(location, rec);
                _host.showProperties(this, true);
                Timber.e("使用外部查看器查看图片: %s", inplace);
            } else {
                _host.showPhoto(rec, true);
                Timber.e("使用内部查看器查看图片: %s", inplace);
            }
        }
    }


    /**
     * 使用ZIP文件浏览器打开
     */
    private void openZIP() {
        // 压缩文件处理
        // Uri devUri = _loc.getDeviceAccessibleUri(this.getPath()); 不受配置影响强制使用 content provide
        Uri devUri = MainContentProvider.getContentUriFromLocation(_loc, this.getPath());
        Intent intent = new Intent();
        intent.putExtra("uri", devUri);
        intent.setClass(_host.getBaseContext(), ZipFileActivity.class);
        _host.startActivity(intent);
    }

    /**
     * 使用MPV播放器播放
     */
    private void openMPV() {
        // Uri devUri = _loc.getDeviceAccessibleUri(this.getPath()); 不受配置影响强制使用 content provide
        /*Uri devUri = MainContentProvider.getContentUriFromLocation(_loc, this.getPath());
        Intent i = new Intent(Intent.ACTION_VIEW, devUri);
        i.setClass(_host.requireActivity(), PlayerActivity.class);
        _host.startActivity(i);*/
    }

    /**
     * 使用IJK播放器播放
     */
    private void openIJK() {
        // IJK 播放器
        // Uri devUri = _loc.getDeviceAccessibleUri(this.getPath()); 不受配置影响强制使用 content provide
        Uri devUri = MainContentProvider.getContentUriFromLocation(_loc, this.getPath());
        Intent intent = new Intent();
        intent.putExtra("uri", devUri);
        intent.setClass(_host.getBaseContext(), VideoActivity.class);
        _host.startActivity(intent);
    }

    /**
     * 压缩文件处理
     */
    private void openRAR() {
        // 压缩文件处理
        // Uri devUri = _loc.getDeviceAccessibleUri(this.getPath()); 不受配置影响强制使用 content provide
        Uri devUri = MainContentProvider.getContentUriFromLocation(_loc, this.getPath());
        Intent intent = new Intent();
        intent.putExtra("uri", devUri);
        intent.setClass(_host.getBaseContext(), RarFileActivity.class);
        _host.startActivity(intent);
    }
}