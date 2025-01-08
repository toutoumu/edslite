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
import com.sovworks.eds.android.filemanager.fragments.FileListViewFragment;
import com.sovworks.eds.fs.util.StringPathUtil;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

public class RenameFileDialog extends RxDialogFragment {
    public static final String TAG = "RenameFileDialog";

    public static void showDialog(FragmentManager fm, String currentPath, String fileName) {
        DialogFragment newFragment = new RenameFileDialog();
        Bundle b = new Bundle();
        b.putString(ARG_CURRENT_PATH, currentPath);
        b.putString(ARG_FILENAME, fileName);
        newFragment.setArguments(b);
        newFragment.show(fm, TAG);
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        // Set an EditText view to get user input
        final String filename = getArguments().getString(ARG_FILENAME);

        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Inflater is null");
        }
        View v = inflater.inflate(R.layout.dialog_edit_text, null);
        TextInputLayout inputLayout = v.findViewById(R.id.text_input_layout);
        inputLayout.setHint(R.string.enter_new_file_name);

        TextInputEditText input = v.findViewById(android.R.id.text1);
        input.setSingleLine();
        input.setText(filename);
        input.setHint(R.string.enter_new_file_name);
        StringPathUtil spu = new StringPathUtil(filename);
        String fnWoExt = spu.getFileNameWithoutExtension();
        if (!fnWoExt.isEmpty()) {
            input.setSelection(0, fnWoExt.length());
        }

        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(requireActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        alert.setTitle(getString(R.string.rename));
        alert.setView(v);
        alert.setPositiveButton(getString(android.R.string.ok), (dialog, whichButton) -> renameFile(input.getText().toString()));
        alert.setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
            // Canceled.
        });
        return alert.create();
    }

    private static final String ARG_CURRENT_PATH = "com.sovoworks.eds.android.PATH";
    private static final String ARG_FILENAME = "com.sovoworks.eds.android.FILENAME";

    private void renameFile(String newName) {
        FileListViewFragment frag = (FileListViewFragment) getFragmentManager().findFragmentByTag(FileListViewFragment.TAG);
        if (frag != null) {
            String prevName = getArguments().getString(ARG_CURRENT_PATH);
            frag.renameFile(prevName, newName);
        }
    }
}
