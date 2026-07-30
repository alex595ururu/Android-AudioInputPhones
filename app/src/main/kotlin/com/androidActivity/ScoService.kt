package com.androidActivity

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log

class ScoService : Service() {

    companion object {
        const val TAG = "ScoService"
        const val CHANNEL_ID = "sco_channel"
        const val NOTIFICATION_ID = 1

        const val ACTION_SCO_STATUS = "com.androidActivity.SCO_STATUS"
        const val EXTRA_STATUS = "status"

        const val STATUS_CONNECTING = "connecting"
        const val STATUS_CONNECTED = "connected"
        const val STATUS_DISCONNECTED = "disconnected"
        const val STATUS_ERROR = "error"
        const val STATUS_NO_DEVICE = "no_device"
    }

    private lateinit var audioManager: AudioManager
    private var scoReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = buildNotification("Подключение к наушникам...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startSco()
        return START_STICKY
    }

    private fun startSco() {
        sendStatus(STATUS_CONNECTING)

        // Register receiver to track SCO connection state changes
        scoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                when (state) {
                    AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                        Log.i(TAG, "SCO Connected — headphone mic + noise cancellation active")
                        sendStatus(STATUS_CONNECTED)
                        updateNotification("Шумоподавление наушников активно ✓")
                    }
                    AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                        Log.i(TAG, "SCO Disconnected")
                    }
                    AudioManager.SCO_AUDIO_STATE_ERROR -> {
                        Log.e(TAG, "SCO Error")
                        sendStatus(STATUS_ERROR)
                        updateNotification("Ошибка подключения SCO")
                    }
                }
            }
        }

        val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scoReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(scoReceiver, filter)
        }

        // Set communication mode — tells the OS a "call" is active
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

        // Modern API (Android 12+): setCommunicationDevice
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val devices = audioManager.availableCommunicationDevices
            val scoDevice = devices.find { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
            if (scoDevice != null) {
                val result = audioManager.setCommunicationDevice(scoDevice)
                Log.i(TAG, "setCommunicationDevice(BT_SCO) = $result")
                if (result) {
                    sendStatus(STATUS_CONNECTED)
                    updateNotification("Шумоподавление наушников активно ✓")
                    return
                }
            } else {
                Log.w(TAG, "No BT SCO device found via modern API, trying legacy")
            }
        }

        // Legacy API fallback
        if (audioManager.isBluetoothScoAvailableOffCall) {
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
            Log.i(TAG, "startBluetoothSco() called (legacy)")
        } else {
            Log.e(TAG, "Bluetooth SCO not available")
            sendStatus(STATUS_NO_DEVICE)
            updateNotification("Bluetooth-наушники не найдены")
        }
    }

    private fun stopSco() {
        // Clear modern API device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try { audioManager.clearCommunicationDevice() } catch (e: Exception) {
                Log.e(TAG, "clearCommunicationDevice error", e)
            }
        }

        // Stop legacy SCO
        try {
            audioManager.isBluetoothScoOn = false
            audioManager.stopBluetoothSco()
        } catch (e: Exception) {
            Log.e(TAG, "stopBluetoothSco error", e)
        }

        // Reset audio mode to normal
        audioManager.mode = AudioManager.MODE_NORMAL

        // Unregister receiver
        scoReceiver?.let {
            try { unregisterReceiver(it) } catch (_: Exception) {}
        }
        scoReceiver = null

        sendStatus(STATUS_DISCONNECTED)
    }

    override fun onDestroy() {
        stopSco()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- Helpers ---

    private fun sendStatus(status: String) {
        val intent = Intent(ACTION_SCO_STATUS).apply {
            putExtra(EXTRA_STATUS, status)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bluetooth SCO",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Поддержание режима шумоподавления наушников"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainKotlin::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Audio Input Phones")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = buildNotification(text)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
