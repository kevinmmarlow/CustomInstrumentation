package com.kmarlow.custominstrumentation;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class OverlayDialogActivity extends AppCompatActivity implements View.OnClickListener {
    private Button okayButton;
    private Button cancelButton;

    @Override
    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_overlay_dialog);
        this.setFinishOnTouchOutside(false);
        okayButton = (Button) findViewById(R.id.ok);
        cancelButton = (Button) findViewById(R.id.cancel);

        okayButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok:
                startSettings();
                break;
            case R.id.cancel:
            default:
                stopService(new Intent(this, AndromiumControllerService.class));
                finish();
        }
    }

    public void startSettings() {
        Toast.makeText(this, "Andromium needs the app to have OverlayPermission", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        stopService(new Intent(this, AndromiumControllerService.class));
        finish();
    }
}
