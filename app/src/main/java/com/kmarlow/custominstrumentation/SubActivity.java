package com.kmarlow.custominstrumentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ActionBarOverlayLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Run this to print out the stack.
 * adb shell dumpsys activity activities | sed -En -e '/ADMStack #/p' -e '/Running activities/,/Run #0/p'
 */
public class SubActivity extends AppCompatActivity {

    private static final String TAG = SubActivity.class.getSimpleName();

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        View decorView = getWindow().getDecorView();
        Log.d("jesse", "this is the decor view: " + decorView);
        ActionBarOverlayLayout overlayLayout = (ActionBarOverlayLayout) decorView.findViewById(R.id.decor_content_parent);
//        Log.d("jesse", "overlay mode: " + overlayLayout.isInOverlayMode());

        Button button = (Button) findViewById(R.id.button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("jesse", "this is from the new click listener");
                    startActivity(new Intent(SubActivity.this, ThirdActivity.class));
                }
            });
        } else {
            Log.d(TAG, "the button is null");
        }

        Button showLink = (Button) findViewById(R.id.show_link);
        if (showLink != null) {
            showLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = "http://kissmanga.com/";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }
            });
        } else {
            Log.d(TAG, "the button is null");
        }

        imageView = (ImageView) findViewById(R.id.image);

        Button loadImage = (Button) findViewById(R.id.load_photo);
        if (loadImage != null) {
            loadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select file to upload "), 1000);
                }
            });
        } else {
            Log.d(TAG, "the button is null");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState called");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("jesse", "this is the data: " + data);
        Log.d("jesse", "this is the get data: " + data.getData());
        Log.d("jesse", "this is the get extra: " + data.getExtras());
        Log.d("jesse", "this is the get extra data: " + data.getExtras().get("data"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");
    }
}
