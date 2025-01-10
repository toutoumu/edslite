package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.settings.GlobalConfig;

public class DrawerHelpMenuItem extends DrawerMenuItemBase {

    public DrawerHelpMenuItem(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    public String getTitle() {
        return getDrawerController().getMainActivity().getString(R.string.help);
    }

    @Override
    public void onClick(View view, int position) {
        super.onClick(view, position);
        getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConfig.HELP_URL)));
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
                // navigationView.setCheckedItem(newMenuItem);
                final MenuItem checkedItem = navigationView.getCheckedItem();
                if (checkedItem != null) {
                    checkedItem.setCheckable(false);
                }
                onClick(item.getActionView(), 0);
                return false;
            }
        });
    }

    private synchronized static Drawable getIcon(Context context) {
        if (_icon == null) {
            // TypedValue typedValue = new TypedValue();
            // context.getTheme().resolveAttribute(R.attr.helpIcon, typedValue, true);
            // noinspection deprecation
            _icon = AppCompatResources.getDrawable(context, R.drawable.ic_help);
        }
        return _icon;
    }

    private static Drawable _icon;

}
