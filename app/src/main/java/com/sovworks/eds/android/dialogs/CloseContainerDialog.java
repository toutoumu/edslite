package com.sovworks.eds.android.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.sovworks.eds.android.R;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class CloseContainerDialog extends RxDialogFragment {
    public static final String TAG = "ProgressDialog";
    public static final String ARG_TITLE = "com.sovworks.eds.android.TITLE";
    private DialogInterface.OnCancelListener _cancelListener;

    public static CloseContainerDialog showDialog(FragmentManager fm, String title) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        CloseContainerDialog d = new CloseContainerDialog();
        d.setArguments(args);
        d.show(fm, TAG);
        return d;
    }

    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        _cancelListener = listener;
    }

    private String getTitle() {
        return getArguments().getString(ARG_TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Inflater is null");
        }
        View v = inflater.inflate(R.layout.dialog_close, null);
        CircularProgressIndicator indicator = v.findViewById(android.R.id.progress);
        MaterialTextView statusTextView = v.findViewById(android.R.id.text1);
        statusTextView.setText(getTitle());

        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        alert.setView(v);
        return alert.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (_cancelListener != null) {
            _cancelListener.onCancel(dialog);
        }
    }
}
