package com.example.dingyu.ui.userinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import com.example.dingyu.R;

/**
 * User: qii
 * Date: 13-3-2
 */
public class SelectProfilePictureDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] items = {getString(R.string.take_camera), getString(R.string.select_pic)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.select))
                .setItems(items, (DialogInterface.OnClickListener) getActivity());
        return builder.create();
    }

}
