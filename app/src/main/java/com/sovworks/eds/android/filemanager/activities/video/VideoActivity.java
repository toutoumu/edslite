package com.sovworks.eds.android.filemanager.activities.video;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.ijk.view.IJKVideoView;

import com.sovworks.eds.android.R;

import java.io.IOException;

/**
 * 视频播放页面
 */
public class VideoActivity extends AppCompatActivity {

    IJKVideoView ijkVideoView;

    private void initIjk() throws IOException {
        ijkVideoView = findViewById(R.id.ijk);
        // 是否是直播源
        ijkVideoView.setLiveSource(false);
        // 播放视频
        Uri url = getIntent().getExtras().getParcelable("uri");
        String source = ijkVideoView.getDataSource();
        if (source == null || source.isEmpty()) {
            ijkVideoView.setDataSource(new StreamDataSource(this, url));
            // ijkVideoView.setDataSource(url); // 这个方法播放部分视频会崩溃
            ijkVideoView.start();
        } else {
            ijkVideoView.reset();
            ijkVideoView.setDataSource(new StreamDataSource(this, url));
            // ijkVideoView.setDataSource(url); // 这个方法播放部分视频会崩溃
            ijkVideoView.prepareAsync();
        }

        // 播放URL
        /*String url = "https://media.w3.org/2010/05/sintel/trailer.mp4";
        ijkVideoView.setDataSource(url);*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // decorFitsSystemWindows true 顶部状态栏空出来, false 内容填充到状态栏下层
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(attributes);
        }

        setContentView(R.layout.activity_video);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        try {
            initIjk();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    finish();
                    return;
                }
                ijkVideoView.toggleOrientation();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkVideoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ijkVideoView != null) {
            ijkVideoView.destroy();
        }
    }
}