package com.sovworks.eds.android.navigdrawer;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.locations.DeviceRootNPLocation;
import com.sovworks.eds.locations.Location;

import java.util.List;
import java.util.function.Consumer;

public class DrawerLocalFilesMenu extends DrawerLocalFilesMenuBase {
    @Override
    void initMenu(NavigationView navigationView, SubMenu subMenu1) {
        final SubMenu subMenu = navigationView.getMenu().addSubMenu(getTitle());

        getSubItems().forEach(new Consumer<DrawerMenuItemBase>() {
            @Override
            public void accept(DrawerMenuItemBase drawerMenuItemBase) {
                MenuItem newMenuItem = subMenu.add(Menu.FIRST, 0, Menu.NONE, drawerMenuItemBase.getTitle());
                newMenuItem.setIcon(drawerMenuItemBase.getIcon());
                newMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(@NonNull MenuItem item) {
                        navigationView.setCheckedItem(newMenuItem);
                        drawerMenuItemBase.onClick(item.getActionView(), 0);
                        return false;
                    }
                });
            }
        });
    }

    public DrawerLocalFilesMenu(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    protected void addLocationMenuItem(List<DrawerMenuItemBase> list, Location loc) {
        if (loc instanceof DeviceRootNPLocation && _allowDeviceLocations) {
            list.add(new DrawerDeviceRootMemoryItem(loc, getDrawerController()));
        } else {
            super.addLocationMenuItem(list, loc);
        }
    }

}
