package com.sovworks.eds.android.settings.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.sovworks.eds.android.Logger;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.settings.PropertyEditor;
import com.sovworks.eds.android.settings.PropertyEditor.Host;
import com.sovworks.eds.android.settings.views.PropertiesView;

public class TextEditDialog extends AppCompatDialogFragment {
    public static final String TAG = "TextEditDialog";
    public static final String ARG_TEXT = "com.sovworks.eds.android.ARG_TEXT";
    public static final String ARG_MESSAGE_ID = "com.sovworks.eds.android.ARG_MESSAGE_ID";
    public static final String ARG_EDIT_TEXT_RES_ID = "com.sovworks.eds.android.EDIT_TEXT_RES_ID";
    private EditText _input;

    @NonNull
    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(getActivity(), R.style.ThemeOverlay_Catalog_MaterialAlertDialog_Centered_FullWidthButtons);
        int mid = getArguments().getInt(ARG_MESSAGE_ID);
        if (mid != 0) {
            alert.setTitle(getString(mid));
        }
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) {
            throw new RuntimeException("Inflater is null");
        }
        View v = inflater.inflate(getArguments().getInt(ARG_EDIT_TEXT_RES_ID, R.layout.settings_edit_text), null);
        TextInputLayout inputLayout = v.findViewById(R.id.text_input_layout);
        _input = v.findViewById(R.id.edit_text);
        if (mid != 0 && getString(mid) != null) {
            inputLayout.setHint(getString(mid));
            // _input.setHint(getString(mid));
        }
        _input.setText(savedInstanceState == null ? getArguments().getString(ARG_TEXT) : savedInstanceState.getString(ARG_TEXT));
        alert.setView(v);
        alert.setPositiveButton(getString(android.R.string.ok), (dialog, whichButton) -> {
            Host host = PropertiesView.getHost(TextEditDialog.this);
            if (host != null) {
                PropertyEditor pe = host.getPropertiesView().getPropertyById(getArguments().getInt(PropertyEditor.ARG_PROPERTY_ID));
                if (pe != null) {
                    try {
                        ((TextResultReceiver) pe).setResult(_input.getText().toString());
                    } catch (Exception e) {
                        Logger.showAndLog(getActivity(), e);
                    }
                }
            }
        });

		/*alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						// Canceled.
					}
				});*/
        return alert.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_TEXT, _input.getText().toString());
    }

    public interface TextResultReceiver {
        void setResult(String text) throws Exception;
    }
}
