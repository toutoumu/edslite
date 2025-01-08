package com.sovworks.eds.android.helpers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.fragments.TaskFragment.Result;
import com.sovworks.eds.android.fragments.TaskFragment.TaskCallbacks;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class ProgressDialogTaskFragmentCallbacks implements TaskCallbacks {
    public static class Dialog extends RxDialogFragment {
        public static final String TAG = "ProgressDialog";
        public static final String ARG_DIALOG_TEXT = "dialog_text";

        public static Dialog newInstance(String dialogText) {
            Bundle args = new Bundle();
            args.putString(ARG_DIALOG_TEXT, dialogText);
            Dialog d = new Dialog();
            d.setArguments(args);
            return d;
        }

        @NonNull
        @Override
        public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater == null) {
                throw new RuntimeException("Inflater is null");
            }
            View v = inflater.inflate(R.layout.dialog_close, null);
            CircularProgressIndicator indicator = v.findViewById(android.R.id.progress);
            MaterialTextView statusTextView = v.findViewById(android.R.id.text1);
            if (getArguments() != null) {
                statusTextView.setText(getArguments().getString(ARG_DIALOG_TEXT));
            }

            MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
            alert.setView(v);
            return alert.create();
        }
    }

    public ProgressDialogTaskFragmentCallbacks(AppCompatActivity context, int dialogTextResId) {
        _context = context;
        _dialogTextResId = dialogTextResId;
    }

    @Override
    public void onPrepare(Bundle args) {

    }

    @Override
    public void onResumeUI(Bundle args) {
        _dialog = initDialog(args);
        if (_dialog != null) {
            _dialog.show(_context.getSupportFragmentManager(), Dialog.TAG);
        }
    }

    @Override
    public void onSuspendUI(Bundle args) {
        if (_dialog != null) {
            _dialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onUpdateUI(Object state) {

    }

    @Override
    public void onCompleted(Bundle args, Result result) {

    }

    protected final AppCompatActivity _context;

    protected DialogFragment initDialog(Bundle args) {
        return Dialog.newInstance(_context.getText(_dialogTextResId).toString());
    }

    private DialogFragment _dialog;
    private final int _dialogTextResId;

}
