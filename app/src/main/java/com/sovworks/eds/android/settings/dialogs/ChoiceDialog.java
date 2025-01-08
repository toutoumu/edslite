package com.sovworks.eds.android.settings.dialogs;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.settings.ChoiceDialogPropertyEditor;
import com.sovworks.eds.android.settings.PropertyEditor;
import com.sovworks.eds.android.settings.views.PropertiesView;
import com.trello.rxlifecycle3.components.support.RxDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ChoiceDialog extends RxDialogFragment {
    public static final String ARG_VARIANTS = "com.sovworks.eds.android.VARIANTS";
    public static final String ARG_TITLE = "com.sovworks.eds.android.TITLE";

    public static final String TAG = "ChoiceDialog";

    public static void showDialog(FragmentManager fm, int propertyId, String title, List<String> variants, String hostFragmentTag) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_VARIANTS, new ArrayList<>(variants));
        args.putString(ARG_TITLE, title);
        args.putInt(PropertyEditor.ARG_PROPERTY_ID, propertyId);
        if (hostFragmentTag != null) {
            args.putString(PropertyEditor.ARG_HOST_FRAGMENT_TAG, hostFragmentTag);
        }
        DialogFragment newFragment = new ChoiceDialog();
        newFragment.setArguments(args);
        newFragment.show(fm, TAG);
    }

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        PropertyEditor.Host host = PropertiesView.getHost(ChoiceDialog.this);
        final ChoiceDialogPropertyEditor pe = (ChoiceDialogPropertyEditor) host.getPropertiesView().getPropertyById(getArguments().getInt(PropertyEditor.ARG_PROPERTY_ID));
        ArrayList<String> variants = getArguments().getStringArrayList(ARG_VARIANTS);
        final String[] strings = variants == null ? new String[0] : variants.toArray(new String[variants.size()]);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons)
                .setTitle(getArguments().getString(ARG_TITLE))
                .setSingleChoiceItems(strings, pe.getSelectedEntry(), (dialog, item) -> {
                    pe.setSelectedEntry(item);
                    dialog.dismiss();
                });
        return builder.create();
    }

}
