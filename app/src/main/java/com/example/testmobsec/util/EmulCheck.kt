package com.example.testmobsec.util

import android.os.Build
import java.io.File

object EmulCheck {

    fun isRunningOnEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86") ||
                Build.MANUFACTURER.contains("Genymotion") ||
                (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
                "google_sdk" == Build.PRODUCT ||
                hasEmulatorFiles() // Adding the check for emulator-specific files
    }

    private fun hasEmulatorFiles(): Boolean {
        val paths = arrayOf(
            "/system/lib/libc_malloc_debug_qemu.so",
            "/sys/qemu_trace",
            "/system/bin/qemu-props",
            "/system/lib/vboxguest.ko",
            "/system/lib/vboxsf.ko"
        )
        return paths.any { File(it).exists() }
    }

}
