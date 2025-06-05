package com.androidActivity;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.res.Configuration;

public class    MainJava extends Activity implements SurfaceHolder.Callback2
{
    String              msg = "AndroidNativeActivity";

    protected void      onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SurfaceView     surfaceView = new SurfaceView(this);

        setContentView(surfaceView);

        SurfaceHolder   holder = surfaceView.getHolder();

        holder.addCallback(this);

        Log.i(msg, "onCreate Called");
    }

    protected void      onStart()
    {
        super.onStart();

        Log.i(msg, "onStart Called");
    }

    protected void      onResume()
    {
        super.onResume();

        Log.i(msg, "onResume Called");
    }

    protected void      onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        Log.i(msg, "savedInstanceState Called");
    }

    protected void      onPause()
    {
        super.onPause();

        Log.i(msg, "onPause Called");
    }

    protected void      onStop()
    {
        super.onStop();

        Log.i(msg, "onStop Called");
    }

    protected void      onDestroy()
    {
        super.onDestroy();

        Log.i(msg, "onDestroy Called");
    }

    public void         onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

        Log.i(msg, "onWindowFocusChanged Called");
    }

    public void         surfaceCreated(@NonNull SurfaceHolder holder) {Log.i(msg, "surfaceCreated Called");}

    public void         surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {Log.i(msg, "surfaceChanged Called");}

    public void         surfaceRedrawNeeded(@NonNull SurfaceHolder holder) {Log.i(msg, "surfaceRedrawNeeded Called");}

    public void         surfaceDestroyed(@NonNull SurfaceHolder holder) {Log.i(msg, "surfaceDestroyed Called");}

    public void         onContentChanged()
    {
        super.onContentChanged();

        Log.i(msg, "onContentChanged Called");
    }

    public void         onConfigurationChanged(@NonNull Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        Log.i(msg, "onConfigurationChanged Called");
    }

    public void         onLowMemory()
    {
        super.onLowMemory();

        Log.i(msg, "onLowMemory Called");
    }
}
