package com.sovworks.eds.android.activities;


import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;

import com.sovworks.eds.android.R;
import com.sovworks.eds.android.dialogs.AboutDialog;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

public class AboutActivity extends RxAppCompatActivity {

    public static final String ABOUT_FRAGMENT_TAG = "com.sovworks.eds.android.locations.ABOUT_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().
                    beginTransaction().
                    add(R.id.container, new AboutDialog(), ABOUT_FRAGMENT_TAG).
                    commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
