package com.androidActivity

import android.os.Bundle
import android.app.Activity
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.content.res.Configuration

class MainKotlin    : Activity(), SurfaceHolder.Callback2
{
    private val     msg = "AndroidNativeActivity"

    override fun    onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val surfaceView = SurfaceView(this)

        setContentView(surfaceView)

        val holder = surfaceView.holder

        holder.addCallback(this)

        Log.i(msg, "onCreate Called")
    }

    override fun    onStart()
    {
        super.onStart()

        Log.i(msg, "onStart Called")
    }

    override fun    onResume()
    {
        super.onResume()

        Log.i(msg, "onResume Called")
    }

    override fun    onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)

        Log.i(msg, "onSaveInstanceState Called")
    }

    override fun    onPause()
    {
        super.onPause()

        Log.i(msg, "onPause Called")
    }

    override fun    onStop()
    {
        super.onStop()

        Log.i(msg, "onStop Called")
    }

    override fun    onDestroy()
    {
        super.onDestroy()

        Log.i(msg, "onDestroy Called")
    }

    override fun    onWindowFocusChanged(hasFocus: Boolean)
    {
        super.onWindowFocusChanged(hasFocus)

        Log.i(msg, "onWindowFocusChanged Called")
    }

    override fun    surfaceCreated(holder: SurfaceHolder) {Log.i(msg, "surfaceCreated Called")}

    override fun    surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {Log.i(msg, "surfaceChanged Called")}

    override fun    surfaceRedrawNeeded(holder: SurfaceHolder) {Log.i(msg, "surfaceRedrawNeeded Called")}

    override fun    surfaceDestroyed(holder: SurfaceHolder) {Log.i(msg, "surfaceDestroyed Called")}

    override fun    onContentChanged()
    {
        super.onContentChanged()

        Log.i(msg, "onContentChanged Called")
    }

    override fun    onConfigurationChanged(newConfig: Configuration)
    {
        super.onConfigurationChanged(newConfig)

        Log.i(msg, "onConfigurationChanged Called")
    }

    @Deprecated("Deprecated in Java")
    override fun    onLowMemory()
    {
        super.onLowMemory()

        Log.i(msg, "onLowMemory Called")
    }
}