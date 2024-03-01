package com.sovworks.eds.android.filemanager.activities;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.ijk.view.IJKVideoView;


import com.sovworks.eds.android.R;

import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    IJKVideoView ijkVideoView;

    private void initIjk() throws IOException {
        ijkVideoView = findViewById(R.id.ijk);
        // 是否是直播源
        ijkVideoView.setLiveSource(false);
        // 播放视频
        Uri url = getIntent().getExtras().getParcelable("uri");
        // String url = "https://media.w3.org/2010/05/sintel/trailer.mp4";
        // 开始播放
        String source = ijkVideoView.getDataSource();
        if (source == null || source.isEmpty()) {
            ijkVideoView.setDataSource(new StreamDataSource(this, url));
            // ijkVideoView.setDataSource(url);
            ijkVideoView.start();
        } else {
            ijkVideoView.reset();
            ijkVideoView.setDataSource(new StreamDataSource(this, url));
            ijkVideoView.prepareAsync();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // decorFitsSystemWindows true 顶部状态栏空出来, false 内容填充到状态栏下层
        WindowCompat.setDecorFitsSystemWindows(this.getWindow(), false);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT
        );
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
    protected void onStart() {
        super.onStart();
        // ijkVideoView.start();
    }

    /* @Override
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
    protected void onStop() {
        super.onStop();
        ijkVideoView.stop();
    }
 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ijkVideoView != null) {
            ijkVideoView.destroy();
        }
    }
}