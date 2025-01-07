package com.sovworks.eds.android.navigdrawer;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.locations.EncFsLocationBase;
import com.sovworks.eds.locations.LocationsManager;
import com.sovworks.eds.locations.ContainerLocation;
import com.sovworks.eds.locations.EDSLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * 菜单 >> (容器)菜单分组的标题
 */
public class DrawerContainersMenu extends DrawerSubMenuBase {
    @Override
    public String getTitle() {
        return getContext().getString(R.string.containers);
    }

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

                // 如果是容器, 且是打开的, 则添加长按事件
                if (drawerMenuItemBase instanceof DrawerLocationMenuItem) {
                    newMenuItem.setActionView(R.layout.navigation_menu_action);
                    final View actionView = newMenuItem.getActionView();
                    if (actionView != null) {
                        // 如果已经打开
                        if (LocationsManager.isOpenableAndOpen(((DrawerLocationMenuItem) drawerMenuItemBase).getLocation())) {
                            final ImageView image = actionView.findViewById(R.id.settings);
                            image.setImageResource(R.drawable.ic_protect);
                            // 则添加关闭事件
                            actionView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((DrawerLocationMenuItem) drawerMenuItemBase).closeLocation();
                                }
                            });
                            // 长按进入设置
                            actionView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    drawerMenuItemBase.onLongClick(v, 0);
                                    return false;
                                }
                            });
                        } else {
                            // 如果未打开, 则添点击进入设置
                            final ImageView image = actionView.findViewById(R.id.settings);
                            image.setImageResource(R.drawable.ic_settings);

                            // 点击进入设置
                            actionView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    drawerMenuItemBase.onLongClick(v, 0);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public DrawerContainersMenu(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    protected Collection<DrawerMenuItemBase> getSubItems() {
        LocationsManager lm = LocationsManager.getLocationsManager(getContext());
        ArrayList<DrawerMenuItemBase> res = new ArrayList<>();
        for (EDSLocation loc : lm.getLoadedEDSLocations(true)) {
            if (loc instanceof ContainerLocation) {
                res.add(new DrawerContainerMenuItem(loc, getDrawerController()));
            } else if (loc instanceof EncFsLocationBase) {
                res.add(new DrawerEncFsMenuItem(loc, getDrawerController()));
            }
        }
        // 容器管理,菜单项
        res.add(new DrawerManageContainersMenuItem(getDrawerController()));

        return res;
    }
}
