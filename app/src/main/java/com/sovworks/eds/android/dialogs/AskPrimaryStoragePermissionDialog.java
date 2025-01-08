package com.sovworks.eds.android.dialogs;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.fragments.ExtStorageWritePermisisonCheckFragment;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class AskPrimaryStoragePermissionDialog extends RxDialogFragment {
    public static void showDialog(FragmentManager fm) {
        DialogFragment newFragment = new AskPrimaryStoragePermissionDialog();
        newFragment.show(fm, "AskPrimaryStoragePermissionDialog");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        builder.setTitle(R.string.tips);
        builder.setMessage(R.string.storage_permission_desc)
                .setPositiveButton(R.string.grant,
                        (dialog, id) ->
                        {
                            dialog.dismiss();
                            ExtStorageWritePermisisonCheckFragment stateFragment = (ExtStorageWritePermisisonCheckFragment) getFragmentManager().findFragmentByTag(ExtStorageWritePermisisonCheckFragment.TAG);
                            if (stateFragment != null) {
                                stateFragment.requestExtStoragePermission();
                            }
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, id) ->
                        {
                            ExtStorageWritePermisisonCheckFragment stateFragment = (ExtStorageWritePermisisonCheckFragment) getFragmentManager().findFragmentByTag(ExtStorageWritePermisisonCheckFragment.TAG);
                            if (stateFragment != null) {
                                stateFragment.cancelExtStoragePermissionRequest();
                            }
                        });
        return builder.create();
    }

}
