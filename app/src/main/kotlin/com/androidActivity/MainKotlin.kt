package com.androidActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainKotlin : Activity() {

    companion object {
        const val REQ_PERMISSIONS = 100
    }

    private lateinit var statusText: TextView
    private lateinit var toggleButton: Button
    private var serviceRunning = false

    // Receive status broadcasts from ScoService
    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getStringExtra(ScoService.EXTRA_STATUS) ?: return
            runOnUiThread { updateStatus(status) }
        }
    }

    // ---- Lifecycle ----

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(ScoService.ACTION_SCO_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(statusReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(statusReceiver) } catch (_: Exception) {}
    }

    // ---- UI ----

    private fun buildUI() {
        val bg = Color.parseColor("#121220")
        val cardBg = Color.parseColor("#1C1C36")
        val textPrimary = Color.parseColor("#E8E8F0")
        val textSecondary = Color.parseColor("#8080A0")
        val accent = Color.parseColor("#2D5FD0")

        // Root
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(bg)
            setPadding(dp(28), dp(60), dp(28), dp(40))
        }

        // Title
        root.addView(TextView(this).apply {
            text = "\uD83C\uDFA7"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 56f)
            gravity = Gravity.CENTER
        }, wrapLp().apply { bottomMargin = dp(12) })

        root.addView(TextView(this).apply {
            text = "Audio Input Phones"
            setTextColor(textPrimary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, wrapLp().apply { bottomMargin = dp(8) })

        root.addView(TextView(this).apply {
            text = "Переключение микрофона\nна Bluetooth-наушники с шумоподавлением"
            setTextColor(textSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
        }, wrapLp().apply { bottomMargin = dp(36) })

        // Status card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
            background = GradientDrawable().apply {
                setColor(cardBg)
                cornerRadius = dp(16).toFloat()
            }
        }

        statusText = TextView(this).apply {
            text = "\u26AA  Не активно"
            setTextColor(textSecondary)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
        }
        card.addView(statusText, wrapLp())

        root.addView(card, matchLp().apply { bottomMargin = dp(36) })

        // Toggle button
        toggleButton = Button(this).apply {
            text = "ВКЛЮЧИТЬ"
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            isAllCaps = false
            stateListAnimator = null
            setPadding(dp(32), dp(18), dp(32), dp(18))
            background = GradientDrawable().apply {
                setColor(accent)
                cornerRadius = dp(14).toFloat()
            }
            setOnClickListener { onToggle() }
        }
        root.addView(toggleButton, wrapLp().apply { bottomMargin = dp(40) })

        // Instructions
        root.addView(TextView(this).apply {
            text = "Как использовать:\n\n" +
                "1. Подключите Bluetooth-наушники\n" +
                "2. Нажмите «ВКЛЮЧИТЬ»\n" +
                "3. Сверните приложение\n" +
                "4. Откройте стандартную камеру\n" +
                "   или диктофон — звук пойдёт\n" +
                "   через микрофон наушников"
            setTextColor(Color.parseColor("#606080"))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            gravity = Gravity.CENTER
            setLineSpacing(dp(3).toFloat(), 1f)
        }, wrapLp())

        setContentView(root)
    }

    // ---- Logic ----

    private fun onToggle() {
        if (serviceRunning) {
            stopService(Intent(this, ScoService::class.java))
            serviceRunning = false
            updateUI(active = false)
        } else {
            if (hasAllPermissions()) {
                startScoService()
            } else {
                requestNeededPermissions()
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun requestNeededPermissions() {
        val perms = mutableListOf(android.Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissions(perms.toTypedArray(), REQ_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQ_PERMISSIONS) {
            // RECORD_AUDIO is critical
            val recordIdx = permissions.indexOf(android.Manifest.permission.RECORD_AUDIO)
            if (recordIdx >= 0 && grantResults[recordIdx] == PackageManager.PERMISSION_GRANTED) {
                startScoService()
            } else {
                statusText.text = "\u274C  Нужно разрешение на микрофон"
                statusText.setTextColor(Color.parseColor("#FF4444"))
            }
        }
    }

    private fun startScoService() {
        val intent = Intent(this, ScoService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        serviceRunning = true
        updateUI(active = true)
    }

    // ---- Status Updates ----

    private fun updateStatus(status: String) {
        when (status) {
            ScoService.STATUS_CONNECTING -> {
                statusText.text = "\uD83D\uDFE1  Подключение..."
                statusText.setTextColor(Color.parseColor("#DDAA00"))
            }
            ScoService.STATUS_CONNECTED -> {
                statusText.text = "\uD83D\uDFE2  Шумоподавление активно"
                statusText.setTextColor(Color.parseColor("#00CC66"))
            }
            ScoService.STATUS_DISCONNECTED -> {
                serviceRunning = false
                updateUI(active = false)
            }
            ScoService.STATUS_ERROR -> {
                statusText.text = "\uD83D\uDD34  Ошибка подключения SCO"
                statusText.setTextColor(Color.parseColor("#FF4444"))
            }
            ScoService.STATUS_NO_DEVICE -> {
                statusText.text = "\uD83D\uDD34  Bluetooth-наушники не найдены"
                statusText.setTextColor(Color.parseColor("#FF6644"))
            }
        }
    }

    private fun updateUI(active: Boolean) {
        val btnBg = toggleButton.background as GradientDrawable
        if (active) {
            toggleButton.text = "ВЫКЛЮЧИТЬ"
            btnBg.setColor(Color.parseColor("#A02020"))
        } else {
            toggleButton.text = "ВКЛЮЧИТЬ"
            btnBg.setColor(Color.parseColor("#2D5FD0"))
            statusText.text = "\u26AA  Не активно"
            statusText.setTextColor(Color.parseColor("#8080A0"))
        }
    }

    // ---- Util ----

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun matchLp() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun wrapLp() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { gravity = Gravity.CENTER_HORIZONTAL }
}