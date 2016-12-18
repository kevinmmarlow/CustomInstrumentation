package com.kmarlow.custominstrumentation;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        getWindow().getDecorView().getLayoutParams().

        findViewById(R.id.nextScreenButton)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showNextScreen();
                    }
                });

        findViewById(R.id.dialogWtf)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showAlertDialog();
                    }
                });

    }

    private void showNextScreen() {
        Log.d("jesse", "this is showing next screen");
        Intent intent = new Intent(this, SubActivity.class);
        startActivity(intent);
    }

    private void showAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("show Alert Dialog")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("jesse", "this si the YESSSS");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("jesse", "this the NOOOO");
                        createAlertDialog();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("create Alert Dialog")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("jesse", "this si the YESSSS");
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("jesse", "this the NOOOO");
                        showDialog();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();

        alertDialog.show();
    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialog.setTitle("Show Dialog");
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("jesse", "this is Dismissing");
                showCustomDialog();
            }
        });
        dialog.show();
    }

    private void showCustomDialog() {
        CustomDialog customDialog = new CustomDialog(this);
        customDialog.setTitle("Show Custom Dialog");
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("jesse", "this is Dismissing");
                showCustomAppCompatDialog();
            }
        });
        customDialog.show();
    }

    private void showCustomAppCompatDialog() {
        CustomAppCompatDialog customDialog = new CustomAppCompatDialog(this);
        customDialog.setTitle("Show Custom App Compat Dialog");
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("jesse", "this is Dismissing");
            }
        });
        customDialog.show();
    }
}
