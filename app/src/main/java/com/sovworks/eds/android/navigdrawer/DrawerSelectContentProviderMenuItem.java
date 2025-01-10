package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.fragments.FileListViewFragment;

public class DrawerSelectContentProviderMenuItem extends DrawerMenuItemBase {
    public DrawerSelectContentProviderMenuItem(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    public String getTitle() {
        return getDrawerController().getMainActivity().getString(R.string.content_provider);
    }

    @Override
    public void onClick(View view, int position) {
        super.onClick(view, position);
        Intent i;
        i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        // if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !getDrawerController().getMainActivity().isSingleSelectionMode())
        //    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        FileListViewFragment f = getDrawerController().getMainActivity().getFileListViewFragment();
        if (f != null) {
            f.startActivityForResult(i, FileListViewFragment.REQUEST_CODE_SELECT_FROM_CONTENT_PROVIDER);
        }
    }

    @Override
    public int getViewType() {
        return 2;
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

    @Override
    public void updateView(View view, @SuppressWarnings("UnusedParameters") int position) {
        super.updateView(view, position);
        ImageView iv = (ImageView) view.findViewById(R.id.close);
        if (iv != null) {
            iv.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.drawer_location_item;
    }

    @Override
    public Drawable getIcon() {
        return getIcon(getDrawerController().getMainActivity());
    }

    private synchronized static Drawable getIcon(Context context) {
        if (_icon == null) {
            // TypedValue typedValue = new TypedValue();
            // context.getTheme().resolveAttribute(R.attr.storageIcon, typedValue, true);
            // noinspection deprecation
            _icon = AppCompatResources.getDrawable(context, R.drawable.ic_storage);
        }
        return _icon;
    }

    private static Drawable _icon;
}
