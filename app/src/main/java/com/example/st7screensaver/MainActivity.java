package com.example.st7screensaver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;

public class MainActivity extends AppCompatActivity {
    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 123;
    private Button button;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askForSystemOverlayPermission();

        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.textView);


        int badge_count = getIntent().getIntExtra("badge_count", 0);

        textView.setText(badge_count + " messages received previously");

        boolean settingsCanWrite = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            settingsCanWrite = Settings.System.canWrite(this);
        }
        //Toast.makeText(this, ""+settingsCanWrite, Toast.LENGTH_SHORT).show();


        if (settingsCanWrite)
        {

            //dimsToZero();

        }
        else
        {
            try {
                if (checkSystemWritePermission()) {

                    //dimsToZero();

                }else {
                    Toast.makeText(this, "Allow modify system settings ==> ON ", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.i("brightness",e.toString());
                Toast.makeText(this, "Unable to change brightness ", Toast.LENGTH_SHORT).show();
            }
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(MainActivity.this)) {
                    startService(new Intent(MainActivity.this, FloatingWidgetService.class));
                    startService(new Intent(MainActivity.this, YourService.class));
                } else {
                    errorToast();
                }
            }
        });

        findViewById(R.id.buttonStop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, FloatingWidgetService.class));
                stopService(new Intent(MainActivity.this, YourService.class));
                FloatingWidgetService floatingWidgetService = new FloatingWidgetService();
                floatingWidgetService.removeOverlay();
            }
        });

    }

    private void askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();


        // To prevent starting the service if the required permission is NOT granted.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            startService(new Intent(MainActivity.this, FloatingWidgetService.class).putExtra("activity_background", true));
            finish();
        } else {
            errorToast();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    errorToast();
                    finish();
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void errorToast() {
        Toast.makeText(this, "Draw over other app permission not available. Can't start the application without the permission.", Toast.LENGTH_LONG).show();
    }

    private boolean checkSystemWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.System.canWrite(this))
                return true;
            else
                openAndroidPermissionsMenu();
        }
        return false;
    }

    private void openAndroidPermissionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + this.getPackageName()));
            this.startActivity(intent);
        }
    }

}


