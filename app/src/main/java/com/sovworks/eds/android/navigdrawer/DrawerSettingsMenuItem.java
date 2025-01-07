package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.settings.activities.ProgramSettingsActivity;

public class DrawerSettingsMenuItem extends DrawerMenuItemBase {
    public DrawerSettingsMenuItem(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    public String getTitle() {
        return getDrawerController().getMainActivity().getString(R.string.settings);
    }

    @Override
    public void onClick(View view, int position) {
        super.onClick(view, position);
        getContext().startActivity(new Intent(getContext(), ProgramSettingsActivity.class));
    }

    @Override
    public Drawable getIcon() {
        return getIcon(getDrawerController().getMainActivity());
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

    private synchronized static Drawable getIcon(Context context) {
        if (_icon == null) {
            // TypedValue typedValue = new TypedValue();
            // context.getTheme().resolveAttribute(R.attr.settingsIcon, typedValue, true);
            // noinspection deprecation
            _icon = context.getResources().getDrawable(R.drawable.ic_settings_dark);
        }
        return _icon;
    }

    private static Drawable _icon;
}
