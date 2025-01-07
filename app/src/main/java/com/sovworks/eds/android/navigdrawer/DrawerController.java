package com.sovworks.eds.android.navigdrawer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.FileManagerActivity;
import com.sovworks.eds.android.filemanager.activities.video.VideoActivity;

import java.util.ArrayList;
import java.util.List;

public class DrawerController extends DrawerControllerBase {
    public DrawerController(FileManagerActivity activity) {
        super(activity);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected List<DrawerMenuItemBase> fillDrawer() {
        NavigationView navigationView = getMainActivity().findViewById(R.id.navigation_view_end);
        Menu menu = navigationView.getMenu();
        menu.clear();

        DrawerLayout drawerLayout = getMainActivity().findViewById(R.id.drawer_layout);
        // CustomDrawerLayout.setCustomLeftEdgeSize(drawerLayout, 1f);
        View content = getMainActivity().findViewById(R.id.linear_layout);
        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                content.setTranslationX(navigationView.getWidth() * slideOffset);
            }
        });

        Intent i = getMainActivity().getIntent();
        boolean isSelectAction = getMainActivity().isSelectAction();
        ArrayList<DrawerMenuItemBase> list = new ArrayList<>();
        DrawerAdapter adapter = new DrawerAdapter(list);
        if (i.getBooleanExtra(FileManagerActivity.EXTRA_ALLOW_BROWSE_CONTAINERS, true)) {
            adapter.add(new DrawerContainersMenu(this));
            new DrawerContainersMenu(this).initMenu(navigationView, null);
        }
        if (i.getBooleanExtra(FileManagerActivity.EXTRA_ALLOW_BROWSE_DEVICE, true)) {
            adapter.add(new DrawerLocalFilesMenu(this));
            new DrawerLocalFilesMenu(this).initMenu(navigationView, null);
        }
        if (!isSelectAction) {
            SubMenu subMenu = menu.addSubMenu(R.string.file_system_type);
            adapter.add(new DrawerSettingsMenuItem(this));
            adapter.add(new DrawerHelpMenuItem(this));
            adapter.add(new DrawerAboutMenuItem(this));
            adapter.add(new DrawerExitMenuItem(this));
            new DrawerSettingsMenuItem(this).initMenu(navigationView, subMenu);
            new DrawerHelpMenuItem(this).initMenu(navigationView, subMenu);
            new DrawerAboutMenuItem(this).initMenu(navigationView, subMenu);
            new DrawerExitMenuItem(this).initMenu(navigationView, subMenu);
        }

        if (getDrawerListView() != null) {
            getDrawerListView().setAdapter(adapter);
        }

        return list;
    }
}
