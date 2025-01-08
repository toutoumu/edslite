package com.sovworks.eds.android.filemanager.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sovworks.eds.android.Logger;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.fragments.FileListViewFragment;
import com.sovworks.eds.fs.Path;
import com.sovworks.eds.fs.util.PathUtil;
import com.sovworks.eds.locations.Location;
import com.sovworks.eds.locations.LocationsManager;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

import java.util.ArrayList;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeleteConfirmationDialog extends RxDialogFragment {
    public static final String TAG = "DeleteConfirmationDialog";

    public static void showDialog(FragmentManager fm, Bundle args) {
        RxDialogFragment newFragment = new DeleteConfirmationDialog();
        newFragment.setArguments(args);
        newFragment.show(fm, TAG);
    }

    @NonNull
    @Override
    @SuppressLint("CheckResult")
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        ArrayList<Path> paths = new ArrayList<>();
        Location loc = LocationsManager.getLocationsManager(getActivity()).getFromBundle(args, paths);
        boolean wipe = args.getBoolean(FileListViewFragment.ARG_WIPE_FILES, false);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        LayoutInflater inflater = (LayoutInflater) builder.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Inflater is null");
        }
        View v = inflater.inflate(R.layout.delete_confirmation_dialog, null);
        MaterialTextView tv = v.findViewById(android.R.id.text1);
        tv.setText(getString(R.string.do_you_really_want_to_delete_selected_files, "..."));
        builder.setTitle("提示")
                .setView(v)
                // .setMessage(getActivity().getString(R.string.do_you_really_want_to_delete_selected_files))
                .setCancelable(true)
                .setPositiveButton(R.string.yes, (dialog, id) -> {
                    FileListViewFragment frag = (FileListViewFragment) getParentFragmentManager().findFragmentByTag(FileListViewFragment.TAG);
                    if (frag != null) {
                        frag.deleteFiles(loc, paths, wipe);
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
        Single.<String>create(c -> {
                    String fn = "";
                    if (loc != null) {
                        if (paths.size() > 1) {
                            fn = String.valueOf(paths.size());
                        } else if (paths.isEmpty()) {
                            fn = PathUtil.getNameFromPath(loc.getCurrentPath());
                        } else {
                            fn = PathUtil.getNameFromPath(paths.get(0));
                        }
                    }
                    c.onSuccess(fn);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fn -> {
                    tv.setText(getString(R.string.do_you_really_want_to_delete_selected_files, fn));
                }, err -> {
                    Logger.showAndLog(getActivity().getApplicationContext(), err);
                });
        return builder.create();
    }
}
