package com.example.testmobsec

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                val text = event.text.toString()
                Log.d("AccessibilityService", "Text changed: $text")
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName.toString()
                Log.d("AccessibilityService", "Window state changed: $packageName")
            }
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