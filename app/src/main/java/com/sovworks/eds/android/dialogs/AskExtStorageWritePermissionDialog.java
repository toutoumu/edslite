package com.sovworks.eds.android.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.locations.opener.fragments.ExternalStorageOpenerFragment;
import com.sovworks.eds.android.locations.opener.fragments.LocationOpenerBaseFragment;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class AskExtStorageWritePermissionDialog extends RxDialogFragment {
    public static void showDialog(FragmentManager fm, String openerTag) {
        Bundle args = new Bundle();
        args.putString(LocationOpenerBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG, openerTag);
        DialogFragment newFragment = new AskExtStorageWritePermissionDialog();
        newFragment.setArguments(args);
        newFragment.show(fm, "AskExtStorageWritePermissionDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        builder.setTitle(R.string.tips);
        builder.setMessage(R.string.ext_storage_write_permission_request)
                .setPositiveButton(R.string.grant,
                        (dialog, id) ->
                        {
                            dialog.dismiss();
                            ExternalStorageOpenerFragment f = getRecFragment();
                            if (f != null) {
                                f.showSystemDialog();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) ->
                        {
                            ExternalStorageOpenerFragment f = getRecFragment();
                            if (f != null) {
                                f.setDontAskPermissionAndOpenLocation();
                            }

                        });
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ExternalStorageOpenerFragment f = getRecFragment();
        if (f != null) {
            f.cancelOpen();
        }
    }

    private ExternalStorageOpenerFragment getRecFragment() {
        return (ExternalStorageOpenerFragment) getFragmentManager().
                findFragmentByTag(
                        getArguments().getString(
                                LocationOpenerBaseFragment.PARAM_RECEIVER_FRAGMENT_TAG
                        )
                );
    }

}
