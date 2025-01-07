package com.sovworks.eds.android.navigdrawer;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.FileManagerActivity;

import java.util.ArrayList;
import java.util.List;

public abstract class DrawerControllerBase {
    public DrawerControllerBase(FileManagerActivity activity) {
        _activity = activity;
    }

    public void init(Bundle savedState) {
        _drawerLayout = _activity.findViewById(R.id.drawer_layout);
        _drawerListView = _activity.findViewById(R.id.left_drawer);
        _navigationView = _activity.findViewById(R.id.navigation_view_end);

        // noinspection deprecation
        _drawerToggle = new ActionBarDrawerToggle(
                _activity,                  /* host Activity */
                _drawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        // Set the drawer toggle as the DrawerListener
        // noinspection deprecation
        _drawerLayout.setDrawerListener(_drawerToggle);

        ActionBar ab = _activity.getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
            ab.setDisplayShowHomeEnabled(false);
        }

        List<DrawerMenuItemBase> list = fillDrawer();

        if (savedState != null) {
            ArrayList<DrawerMenuItemBase> copy = new ArrayList<>(list);
            for (DrawerMenuItemBase item : copy)
                item.restoreState(savedState);
        }

        if (_drawerListView != null) {
            _drawerListView.setChoiceMode(ListView.CHOICE_MODE_NONE);

            _drawerListView.setOnItemClickListener((adapterView, view, i, l) ->
            {
                DrawerMenuItemBase item = (DrawerMenuItemBase) _drawerListView.getItemAtPosition(i);
                if (item != null) {
                    item.onClick(view, i);
                }
            });
            _drawerListView.setOnItemLongClickListener((parent, view, position, id) ->
            {
                DrawerMenuItemBase item = (DrawerMenuItemBase) _drawerListView.getItemAtPosition(position);
                return item != null && item.onLongClick(view, position);
            });
        }
    }

    public void onPostCreate() {
        if (_drawerToggle != null) {
            _drawerToggle.syncState();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (_drawerToggle != null) {
            _drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (_drawerLayout == null) {
            return false;
        }

        if (_drawerListView != null) {
            if (item.getItemId() == android.R.id.home) {
                if (_drawerLayout.isDrawerOpen(_drawerListView)) {
                    _drawerLayout.closeDrawer(_drawerListView);
                } else {
                    _drawerLayout.openDrawer(_drawerListView);
                }
                return true;
            }
        } else {
            if (item.getItemId() == android.R.id.home) {
                if (_drawerLayout.isDrawerOpen(_navigationView)) {
                    _drawerLayout.closeDrawer(_navigationView);
                } else {
                    _drawerLayout.openDrawer(_navigationView);
                }
                return true;
            }
        }
        return false;
    }

    void closeDrawer() {
        if (_drawerListView != null) {
            _drawerLayout.closeDrawer(_drawerListView);
        } else if (_navigationView != null) {
            _drawerLayout.closeDrawer(_navigationView);
        }
    }

    public void openDrawer() {
        if (_drawerListView != null) {
            _drawerLayout.openDrawer(_drawerListView);
        } else if (_navigationView != null) {
            _drawerLayout.openDrawer(_navigationView);
        }
    }

    public FileManagerActivity getMainActivity() {
        return _activity;
    }

    ListView getDrawerListView() {
        return _drawerListView;
    }

    @SuppressWarnings("unused")
    public DrawerLayout getDrawerLayout() {
        return _drawerLayout;
    }

    public boolean onBackPressed() {
        if (_drawerLayout != null && _navigationView != null && _drawerLayout.isDrawerOpen(_navigationView)) {
            _drawerLayout.closeDrawer(_navigationView);
            return true;
        }

        if (_drawerLayout != null && _drawerListView != null && _drawerLayout.isDrawerOpen(_drawerListView)) {
            _drawerLayout.closeDrawer(_drawerListView);
            return true;
        }
        // 去掉左侧菜单,返回按钮折叠选项功能
        /* for (int i = 0; i < _drawerListView.getCount(); i++) {
            DrawerMenuItemBase item = (DrawerMenuItemBase) _drawerListView.getItemAtPosition(i);
            if (item != null && item.onBackPressed())
                return true;
        } */

        return false;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (_drawerListView == null) {
            return;
        }
        saveState(outState);
    }

    public void updateMenuItemViews() {
        ListView lv = getDrawerListView();
        if (lv != null) {
            DrawerAdapter adapter = (DrawerAdapter) lv.getAdapter();
            adapter.notifyDataSetChanged();
        }
    }

    public void reloadItems() {
        if (_drawerListView == null && _navigationView == null) {
            return;
        }
        Bundle b = new Bundle();
        saveState(b);
        List<DrawerMenuItemBase> list = fillDrawer();
        ArrayList<DrawerMenuItemBase> copy = new ArrayList<>(list);
        for (DrawerMenuItemBase item : copy)
            item.restoreState(b);
    }

    public void showContainers() {
        openDrawer();
        if (_drawerListView == null) {
            return;
        }
        DrawerAdapter da = (DrawerAdapter) _drawerListView.getAdapter();
        for (int i = 0, l = da.getCount(); i < l; i++) {
            DrawerMenuItemBase item = da.getItem(i);
            if (item instanceof DrawerContainersMenu) {
                DrawerContainersMenu dcm = (DrawerContainersMenu) item;
                if (!dcm.isExpanded()) {
                    dcm.rotateIconAndChangeState(da.getView(i, dcm.findView(_drawerListView), _drawerListView));
                }
            }
        }

    }

    protected List<DrawerMenuItemBase> fillDrawer() {
        Intent i = getMainActivity().getIntent();
        boolean isSelectAction = getMainActivity().isSelectAction();
        ArrayList<DrawerMenuItemBase> list = new ArrayList<>();
        DrawerAdapter adapter = new DrawerAdapter(list);
        if (i.getBooleanExtra(FileManagerActivity.EXTRA_ALLOW_BROWSE_CONTAINERS, true)) {
            adapter.add(new DrawerContainersMenu(this));
        }
        if (i.getBooleanExtra(FileManagerActivity.EXTRA_ALLOW_BROWSE_DEVICE, true)) {
            adapter.add(new DrawerLocalFilesMenu(this));
        }
        if (!isSelectAction) {
            adapter.add(new DrawerSettingsMenuItem(this));
            adapter.add(new DrawerHelpMenuItem(this));
            adapter.add(new DrawerAboutMenuItem(this));
            adapter.add(new DrawerExitMenuItem(this));
        }
        if (_drawerListView != null) {
            _drawerListView.setAdapter(adapter);
        }
        return list;
    }

    protected class DrawerAdapter extends ArrayAdapter<DrawerMenuItemBase> {

        DrawerAdapter(List<DrawerMenuItemBase> itemsList) {
            super(_activity, R.layout.drawer_folder, itemsList);
        }

        @Override
        public int getItemViewType(int position) {
            DrawerMenuItemBase rec = getItem(position);
            return rec == null ? 0 : rec.getViewType();
        }

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final DrawerMenuItemBase rec = getItem(position);
            View v;
            if (convertView != null) {
                v = convertView;
                rec.updateView(v, position);
            } else {
                v = rec.createView(position, parent);
            }
            v.setTag(rec);
            return v;
        }

    }

    private final FileManagerActivity _activity;
    private ListView _drawerListView;
    private NavigationView _navigationView;
    private DrawerLayout _drawerLayout;

    @SuppressWarnings("deprecation")
    private ActionBarDrawerToggle _drawerToggle;

    private void saveState(Bundle outState) {
        if (_drawerListView == null) {
            return;
        }
        for (int i = 0; i < _drawerListView.getCount(); i++) {
            DrawerMenuItemBase item = (DrawerMenuItemBase) _drawerListView.getItemAtPosition(i);
            if (item != null) {
                item.saveState(outState);
            }
        }
    }
}
