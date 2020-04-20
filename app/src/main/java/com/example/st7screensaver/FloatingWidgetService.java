package com.example.st7screensaver;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.andremion.counterfab.CounterFab;

public class FloatingWidgetService extends Service {


    private WindowManager mWindowManager;
    private View mOverlayView;
    int mWidth;
    CounterFab counterFab;
    boolean activity_background;
    private boolean status ;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            activity_background = intent.getBooleanExtra("activity_background", false);

        }

        if (mOverlayView == null) {

            mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);



            final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);


            //Specify the view position
            params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;


            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mWindowManager.addView(mOverlayView, params);

            Display display = mWindowManager.getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);

            counterFab = (CounterFab) mOverlayView.findViewById(R.id.fabHead);
            counterFab.setCount(1);



            final RelativeLayout layout = (RelativeLayout) mOverlayView.findViewById(R.id.layout);
            ViewTreeObserver vto = layout.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = layout.getMeasuredWidth();

                    //To get the accurate middle of the screen we subtract the width of the floating widget.
                    mWidth = size.x - width;

                }
            });



            counterFab.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;


                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;


                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            //Toast.makeText(this, ""+settingsCanWrite, Toast.LENGTH_SHORT).show();

                                if (!status)
                                {
                                    dimsToZero();
                                    status=true;
                                    Log.d("status1", String.valueOf(status));
                                }
                                else
                                {
                                    dimsTo100();
                                    status=false;
                                    Log.d("status2", String.valueOf(status));
                                }
                            Log.d("status3", String.valueOf(status));

                            //Toast.makeText(FloatingWidgetService.this, "Clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        case MotionEvent.ACTION_UP:

                            //Only start the activity if the application is in background. Pass the current badge_count to the activity
                            if (activity_background) {

                                float xDiff = event.getRawX() - initialTouchX;
                                float yDiff = event.getRawY() - initialTouchY;

                                if ((Math.abs(xDiff) < 5) && (Math.abs(yDiff) < 5)) {
                                    Intent intent = new Intent(FloatingWidgetService.this, MainActivity.class);
                                    intent.putExtra("badge_count", counterFab.getCount());
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);

                                    //close the service and remove the fab view
                                    stopSelf();
                                }

                            }
                            //Logic to auto-position the widget based on where it is positioned currently w.r.t middle of the screen.
                            int middle = mWidth / 2;
                            float nearestXWall = params.x >= middle ? mWidth : 0;
                            params.x = (int) nearestXWall;


                            mWindowManager.updateViewLayout(mOverlayView, params);


                            return true;
                        case MotionEvent.ACTION_MOVE:


                            int xDiff = Math.round(event.getRawX() - initialTouchX);
                            int yDiff = Math.round(event.getRawY() - initialTouchY);


                            //Calculate the X and Y coordinates of the view.
                            params.x = initialX + xDiff;
                            params.y = initialY + yDiff;

                            //Update the layout with new X & Y coordinates
                            mWindowManager.updateViewLayout(mOverlayView, params);

                            return true;
                    }
                    return false;
                }
            });
        } else {

            counterFab.increase();

        }


        return super.onStartCommand(intent, flags, startId);


    }

    @Override
    public void onCreate() {
        super.onCreate();

        setTheme(R.style.AppTheme);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);

         */


    }

    private void dimsTo100() {

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                (int) 30);

    }

    private void dimsToZero() {

        android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                (int) 0);

        /*
        //7s delaying
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        },7000);

         */
    }

    public void removeOverlay()
    {
        if (mOverlayView != null)
            mWindowManager.removeView(mOverlayView);

    }


}
