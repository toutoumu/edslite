package com.sovworks.eds.android.dialogs;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.helpers.Util;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;


public abstract class ConfirmationDialog extends RxDialogFragment {
    public static final String ARG_RECEIVER_TAG = "com.sovworks.eds.android.RECEIVER_TAG";

    public interface Receiver {
        void onYes();

        void onNo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.setDialogStyle(this);
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        builder.setMessage(getTitle())
                .setTitle(R.string.tips)
                .setCancelable(false)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    onYes();
                    dismiss();
                })
                .setNegativeButton(R.string.no, (dialog, id) -> {
                    onNo();
                    dismiss();
                });
        return builder.create();
    }

    protected void onNo() {
        Receiver rec = getReceiver();
        if (rec != null) {
            rec.onNo();
        }
    }

    protected void onYes() {
        Receiver rec = getReceiver();
        if (rec != null) {
            rec.onYes();
        }
    }

    protected abstract String getTitle();

    protected Receiver getReceiver() {
        Bundle args = getArguments();
        String tag = args == null ? null : args.getString(ARG_RECEIVER_TAG);
        if (tag != null) {
            Fragment f = getFragmentManager().findFragmentByTag(tag);
            if (f instanceof Receiver) {
                return (Receiver) f;
            }
        } else {
            Activity act = getActivity();
            if (act instanceof Receiver) {
                return (Receiver) act;
            }
        }
        return null;
    }
}
