package com.amigos.attendance.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.amigos.attendance.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogUtility {

    public void getMeterialDialog(Context context, String messageTitle, String messageText, String messageType){

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.CustomDialogStyle);

        builder.setTitle(messageTitle)
                .setMessage(messageText)
                .setBackground(ContextCompat.getDrawable(context, R.drawable.dialog_bg));

        if(messageType.equalsIgnoreCase("info")){
            builder.setIcon(R.drawable.info_icon);
        }
        else if(messageType.equalsIgnoreCase("warning")){
            builder.setIcon(R.drawable.warning_icon);
        }
        else if(messageType.equalsIgnoreCase("error")){
            builder.setIcon(R.drawable.error_icon);
        }
        else{

        }

        AlertDialog dialog = builder.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, ApplicationConstants.DELAY_DIALOG_DISAPPEAR);

        if (dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setAttributes(window.getAttributes());

            // Example fade in
            window.getDecorView().setAlpha(0f);
            window.getDecorView().animate().alpha(1f).setDuration(ApplicationConstants.DELAY_DIALOG_FADE_IN).start();
        }

    }

    public AlertDialog.Builder getAlertDialog(Context context, String messageTitle, String messageText, String messageType){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.CustomDialogStyle);
        dialogBuilder.setTitle(messageTitle);
        dialogBuilder.setMessage(messageText);
        dialogBuilder.setCancelable(false);

        if(messageType.equalsIgnoreCase("info")){
            dialogBuilder.setIcon(R.drawable.info_icon);
        }
        else if(messageType.equalsIgnoreCase("warning")){
            dialogBuilder.setIcon(R.drawable.warning_icon);
        }
        else if(messageType.equalsIgnoreCase("error")){
            dialogBuilder.setIcon(R.drawable.error_icon);
        }

        return dialogBuilder;
    }

    public AlertDialog.Builder getEnterMobileDialog(Context context){

        View dialogView = ((Activity)context).getLayoutInflater().inflate(R.layout.enter_mobile_no_dialog, null);
        EditText enterMobile_editText = dialogView.findViewById(R.id.enterMobile_editText);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
                .setTitle("Mobile No")
                .setView(dialogView)
                .setIcon(R.drawable.mobile_icon2)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Get PIN", null)
                .setCancelable(false);


        return dialogBuilder;
    }

}
