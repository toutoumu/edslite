package com.sovworks.eds.android.filemanager.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.tasks.CreateNewFile;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class NewFileDialog extends RxDialogFragment {
    public interface Receiver {
        void makeNewFile(String name, int type);
    }

    private static final String ARG_TYPE = "com.sovworks.eds.android.TYPE";
    private static final String ARG_RECEIVER_TAG = "com.sovworks.eds.android.RECEIVER_TAG";

    public static void showDialog(FragmentManager fm, int type, String receiverTag) {
        DialogFragment newFragment = new NewFileDialog();
        Bundle b = new Bundle();
        b.putInt(ARG_TYPE, type);
        b.putString(ARG_RECEIVER_TAG, receiverTag);
        newFragment.setArguments(b);
        newFragment.show(fm, "NewFileDialog");
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        int ft = getArguments().getInt(ARG_TYPE);

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Inflater is null");
        }
        View v = inflater.inflate(R.layout.dialog_edit_text, null);
        TextInputLayout inputLayout = v.findViewById(R.id.text_input_layout);
        inputLayout.setHint(getString(ft == CreateNewFile.FILE_TYPE_FOLDER ? R.string.enter_new_folder_name : R.string.enter_new_file_name));

        TextInputEditText input = v.findViewById(android.R.id.text1);
        input.setSingleLine();
        // input.setHint(getString(ft == CreateNewFile.FILE_TYPE_FOLDER ? R.string.enter_new_folder_name : R.string.enter_new_file_name));

        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        alert.setView(v);
        alert.setTitle(getString(ft == CreateNewFile.FILE_TYPE_FOLDER ? R.string.new_dir : R.string.new_file));
        alert.setPositiveButton(getString(android.R.string.ok), (dialog, whichButton) -> {
            Receiver r = (Receiver) getParentFragmentManager().findFragmentByTag(getArguments().getString(ARG_RECEIVER_TAG));
            if (r != null) {
                r.makeNewFile(input.getText().toString(), getArguments().getInt(ARG_TYPE));
            }
            dialog.dismiss();
        });
        alert.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
            // Canceled.
        });
        return alert.create();
    }
}
