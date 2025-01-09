package com.sovworks.eds.android.filemanager.records;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaDataSource;
import android.media.MediaMetadataRetriever;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textview.MaterialTextView;
import com.sovworks.eds.android.Logger;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.zip.FileUtils;
import com.sovworks.eds.android.helpers.EdsMediaMetadataSource;
import com.sovworks.eds.android.helpers.ExtendedFileInfoLoader;
import com.sovworks.eds.android.helpers.Util;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.android.settings.UserSettings;
import com.sovworks.eds.fs.File;
import com.sovworks.eds.fs.Path;
import com.sovworks.eds.fs.util.StringPathUtil;
import com.sovworks.eds.settings.GlobalConfig;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * 文件管理>>文件基类
 */
class FileRecord extends FsBrowserRecord {
    public static class ExtFileInfo implements ExtendedFileInfoLoader.ExtendedFileInfo {
        Drawable mainIcon;

        @Override
        public void attach(BrowserRecord record) {
            FileRecord fr = (FileRecord) record;
            _records.add(fr);
            fr._mainIcon = mainIcon;
            fr._needLoadExtInfo = false;
            FileRecord.updateRowView(fr.getHostFragment(), fr);
        }

        @Override
        public void detach(BrowserRecord record) {
            _records.remove(record);
        }

        @Override
        public void clear() {
            for (BrowserRecord r : _records) {
                FileRecord fr = (FileRecord) r;
                RowViewInfo rvi = FileRecord.getCurrentRowViewInfo(fr.getHostFragment(), fr);
                if (rvi != null) {
                    AppCompatImageView iv = rvi.view.findViewById(android.R.id.icon);
                    iv.setImageDrawable(null);
                    iv.setImageBitmap(null);
                    FileRecord.updateRowView(rvi);
                }
            }
            mainIcon = null;
        }

        private final List<BrowserRecord> _records = new ArrayList<>();

    }

    // private static Drawable _fileIcon;
    private Drawable _fileIcon;
    private final boolean _loadPreviews;
    protected boolean _needLoadExtInfo;
    private int _iconWidth = 40, _iconHeight = 40;
    private String _infoString;
    private Drawable _mainIcon;
    private boolean _animateIcon;
    private String mime;

    public FileRecord(Context context) {
        super(context);
        _loadPreviews = _needLoadExtInfo = UserSettings.getSettings(context).showPreviews();
        DisplayMetrics dm = _context.getResources().getDisplayMetrics();
        _iconWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GlobalConfig.FB_PREVIEW_WIDTH, dm);
        _iconHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, GlobalConfig.FB_PREVIEW_HEIGHT, dm);

    }

    @Override
    public void init(Path path) throws IOException {
        super.init(path);
        mime = FileOpsService.getMimeTypeFromExtension(_context, new StringPathUtil(getName()).getFileExtension());
        updateFileInfoString();
    }

    @Override
    public boolean allowSelect() {
        return _host.allowFileSelect();
    }

    @Override
    public void updateView(View view, final int position) {
        super.updateView(view, position);
        MaterialTextView tv = view.findViewById(android.R.id.text2);
        if (_infoString != null) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(_infoString);
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
        AppCompatImageView iv = view.findViewById(android.R.id.icon);

        if (mime.startsWith("image/")) {
            // 图片加载使用Glide
            Glide.with(iv).load(this.getPath())
                    .apply(_host.getFileListViewFragment().getmThumbnailRequestOptions())
                    .placeholder(R.drawable.ic_file_image)
                    .error(R.drawable.ic_default_image_list)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e,
                                                    Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource,
                                                       Object model,
                                                       Target<Drawable> target,
                                                       DataSource dataSource,
                                                       boolean isFirstResource) {
                            iv.setPadding(0, 0, 0, 0);
                            iv.setScaleType(AppCompatImageView.ScaleType.CENTER_CROP);
                            return false;
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(iv);
        } else {
            // 加载文件的预览图,或默认图标
            if (_mainIcon != null) {
                iv.setPadding(0, 0, 0, 0);
                iv.setScaleType(AppCompatImageView.ScaleType.CENTER_CROP);
                iv.setImageDrawable(_mainIcon);
                if (_animateIcon) {
                    iv.startAnimation(AnimationUtils.loadAnimation(_context, R.anim.restore));
                    _animateIcon = false;
                }
            }
        }
    }

    @Override
    public ExtendedFileInfoLoader.ExtendedFileInfo loadExtendedInfo() {
        ExtFileInfo res = new ExtFileInfo();
        initExtFileInfo(res);
        return res;
    }

    @Override
    public boolean needLoadExtendedInfo() {
        return _needLoadExtInfo && mime.startsWith("video/");
    }

    @Override
    protected Drawable getDefaultIcon() {
        return getFileIcon(_host);
    }

    protected void updateFileInfoString() {
        if (_path != null) {
            try {
                _infoString = formatInfoString(_context);
            } catch (IOException ignored) {
            }
        } else {
            _infoString = null;
        }
    }

    protected String formatInfoString(Context context) throws IOException {
        StringBuilder sb = new StringBuilder();
        appendModDataInfo(context, sb);
        appendSizeInfo(context, sb);
        return sb.toString();
    }

    protected void appendSizeInfo(Context context, StringBuilder sb) {
        // sb.append(String.format("%s: %s", context.getText(R.string.size), Formatter.formatFileSize(context, getSize())));
        sb.append(Formatter.formatFileSize(context, getSize()));
    }

    protected void appendModDataInfo(Context context, StringBuilder sb) {
        Date md = getModificationDate();
        if (md != null) {
            /*java.text.DateFormat df = android.text.format.DateFormat.getDateFormat(context);
            java.text.DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
            sb.append(String.format(" %s: %s %s", context.getText(R.string.last_modified), df.format(md), tf.format(md)));*/
            LocalDateTime localDateTime = md.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.getDefault());
            sb.append(localDateTime.format(formatter));
            sb.append("  ");
        }
    }

    protected void initExtFileInfo(ExtFileInfo info) {
        if (_loadPreviews) {
            info.mainIcon = loadMainIcon();
        }
    }

    protected Drawable loadMainIcon() {
        Drawable res = null;
        try {
            if (mime.startsWith("image/")) {
                Timber.e("加载图片用Glide 了");
                res = getImagePreview(_path);
                _animateIcon = true;
            } else if (mime.startsWith("video/")) {
                res = getVideoPreview(_path);
                _animateIcon = true;
            } else {
                res = getDefaultAppIcon(mime);
                _animateIcon = true;
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return res;
    }

    protected Drawable getImagePreview(Path path) throws IOException {
        Bitmap bitmap = Util.loadDownsampledImage(path, _iconWidth, _iconHeight);
        return bitmap != null ? new BitmapDrawable(_context.getResources(), bitmap) : null;
    }
    // 原来的默认图片
    /*private static synchronized Drawable getFileIcon(Context context) {
        if (_fileIcon == null && context != null) {
            _fileIcon = AppCompatResources.getDrawable(context, R.drawable.ic_file);
        }
        return _fileIcon;
    }*/

    private Drawable getFileIcon(Context context) {
        if (_fileIcon == null && context != null) {
            _fileIcon = FileUtils.getFileIcon(context, _path);
        }
        return _fileIcon;
    }

    private Drawable getDefaultAppIcon(String mime) {
        if (mime.equals("*/*")) {
            return null;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(mime);
        // intent.setDataAndType(Uri.parse(path), mime);
        PackageManager pacMan = _context.getPackageManager();
        try {
            final List<ResolveInfo> matches = pacMan.queryIntentActivities(intent, 0);
            for (ResolveInfo match : matches) {
                final Drawable icon = match.loadIcon(pacMan);
                if (icon != null) {
                    return icon;// drawableToBitmap(icon);
                }
            }
        } catch (NullPointerException ignored) {
            // bug?
            // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String android.net.Uri.getHost()' on a null object reference
            // at android.os.Parcel.readException(Parcel.java:1552)
        }
        return null;
    }

    private Drawable getVideoPreview(Path path) throws IOException {
        Bitmap bitmap;
        try (MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever()) {
            MediaDataSource dataSource = new EdsMediaMetadataSource(path.getFile().getRandomAccessIO(File.AccessMode.Read));
            metadataRetriever.setDataSource(dataSource);
            bitmap = metadataRetriever.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_PREVIOUS_SYNC, _iconWidth, _iconHeight);
            metadataRetriever.release();
        }
        return bitmap != null ? new BitmapDrawable(_context.getResources(), bitmap) : null;
    }
}