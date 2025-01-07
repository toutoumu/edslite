package com.sovworks.eds.android.navigdrawer;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.locations.DocumentTreeLocation;

public class DrawerManageLocalStorages extends DrawerManageLocationMenuItem {
    public DrawerManageLocalStorages(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    protected String getLocationType() {
        return DocumentTreeLocation.URI_SCHEME;
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.manage_local_storages);
    }

    @Override
    void initMenu(NavigationView navigationView, SubMenu subMenu) {
        MenuItem newMenuItem = subMenu.add(Menu.FIRST, 0, Menu.NONE, getTitle());
        newMenuItem.setIcon(getIcon());
        newMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                navigationView.setCheckedItem(newMenuItem);
                onClick(item.getActionView(), 0);
                return false;
            }
        });
    }

}
