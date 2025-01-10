package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.locations.closer.fragments.LocationCloserBaseFragment;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.locations.Location;
import com.sovworks.eds.locations.LocationsManager;

import java.util.Iterator;

public class DrawerExitMenuItem extends DrawerMenuItemBase {

    public static class ExitFragment extends Fragment implements LocationCloserBaseFragment.CloseLocationReceiver {
        public static final String TAG = "com.sovworks.eds.android.ExitFragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            closeNextOrExit();
        }

        @Override
        public void onTargetLocationClosed(Location location, Bundle closeTaskArgs) {
            closeNextOrExit();
        }

        @Override
        public void onTargetLocationNotClosed(Location location, Bundle closeTaskArgs) {

        }

        private void closeNextOrExit() {
            Iterator<Location> it = LocationsManager.getLocationsManager(getActivity()).getLocationsClosingOrder().iterator();
            if (it.hasNext()) {
                launchCloser(it.next());
            } else {
                exit();
            }
        }

        private void launchCloser(Location loc) {
            Bundle args = new Bundle();
            args.putString(LocationCloserBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG, TAG);
            LocationsManager.storePathsInBundle(args, loc, null);
            Fragment closer = LocationCloserBaseFragment.getDefaultCloserForLocation(loc);
            closer.setArguments(args);
            getFragmentManager().beginTransaction().add(
                    closer,
                    LocationCloserBaseFragment.getCloserTag(loc)).commit();
        }

        private void exit() {
            FileOpsService.clearTempFolder(getActivity().getApplicationContext(), true);
            getActivity().finish();
        }
    }

    public DrawerExitMenuItem(DrawerControllerBase drawerController) {
        super(drawerController);
    }

    @Override
    public String getTitle() {
        return getDrawerController().getMainActivity().getString(R.string.stop_service_and_exit);
    }

    @Override
    public void onClick(View view, int position) {
        super.onClick(view, position);
        getDrawerController().
                getMainActivity().
                getSupportFragmentManager().
                beginTransaction().
                add(new ExitFragment(), ExitFragment.TAG).
                commit();
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
            // context.getTheme().resolveAttribute(R.attr.exitIcon, typedValue, true);
            // noinspection deprecation
            _icon = AppCompatResources.getDrawable(context, R.drawable.ic_exit);
        }
        return _icon;
    }

    private static Drawable _icon;

}
