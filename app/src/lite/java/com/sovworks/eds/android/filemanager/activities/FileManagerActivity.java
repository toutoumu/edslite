package com.sovworks.eds.android.filemanager.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;

import com.sovworks.eds.android.navigdrawer.DrawerController;
import com.sovworks.eds.locations.Location;

public class FileManagerActivity extends FileManagerActivityBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 文件管理授权
            if (Environment.isExternalStorageManager()) {
                // 已经授权
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 123);
            }
        } else {
            // Android 11之前的版本处理逻辑
        }
    }

    public static void openFileManager(FileManagerActivity fm, Location location, int scrollPosition) {
        fm.goTo(location, scrollPosition);
    }

    @Override
    protected DrawerController createDrawerController() {
        return new DrawerController(this);
    }

    @Override
    protected void showPromoDialogIfNeeded() {
        if (_settings.getLastViewedPromoVersion() < 211)
            super.showPromoDialogIfNeeded();
    }
}
