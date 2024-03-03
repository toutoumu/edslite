package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import com.sovworks.eds.android.R;
import com.sovworks.eds.android.dialogs.AboutDialog;

public class DrawerAboutMenuItem extends DrawerMenuItemBase {

    public DrawerAboutMenuItem(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    public String getTitle() {
        return getDrawerController().getMainActivity().getString(R.string.about);
    }

    @Override
    public void onClick(View view, int position) {
        super.onClick(view, position);
        AboutDialog.showDialog(getDrawerController().getMainActivity().getSupportFragmentManager());
    }

    @Override
    public Drawable getIcon() {
        return getIcon(getDrawerController().getMainActivity());
    }

    private synchronized static Drawable getIcon(Context context) {
        if (_icon == null) {
            // TypedValue typedValue = new TypedValue();
            // context.getTheme().resolveAttribute(R.attr.aboutIcon, typedValue, true);
            // noinspection deprecation
            _icon = context.getResources().getDrawable(R.drawable.ic_about_dark);
        }
        return _icon;
    }

    private static Drawable _icon;

}
