package com.sovworks.eds.android.locations.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.sovworks.eds.android.R;
import com.sovworks.eds.android.helpers.CompatHelper;
import com.sovworks.eds.android.locations.ContainerBasedLocation;
import com.sovworks.eds.android.locations.DocumentTreeLocation;
import com.sovworks.eds.android.locations.fragments.ContainerListFragment;
import com.sovworks.eds.android.locations.fragments.DocumentTreeLocationsListFragment;
import com.sovworks.eds.android.locations.fragments.LocationListBaseFragment;
import com.sovworks.eds.android.settings.UserSettings;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

public abstract class LocationListActivityBase extends RxAppCompatActivity {
    public static final String EXTRA_LOCATION_TYPE = "com.sovworks.eds.android.LOCATION_TYPE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (UserSettings.getSettings(this).isFlagSecureEnabled()) {
            CompatHelper.setWindowFlagSecure(this);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().
                    beginTransaction().
                    add(R.id.container, getCreateLocationFragment(), LocationListBaseFragment.TAG).
                    commit();
        }
    }

    protected Fragment getCreateLocationFragment() {
        switch (getIntent().getStringExtra(EXTRA_LOCATION_TYPE)) {
            case ContainerBasedLocation.URI_SCHEME:
                return new ContainerListFragment();
            case DocumentTreeLocation.URI_SCHEME:
                return new DocumentTreeLocationsListFragment();
            default:
                throw new RuntimeException("Unknown location type");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
