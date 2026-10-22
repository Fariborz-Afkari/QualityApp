package com.example.qualityapp

import android.app.Application
import com.example.qualityapp.data.DebugLog
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@HiltAndroidApp
class QualityApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ── 1) اگر اجرای قبلی کرش کرده، استک‌تریس را داخل DebugLog بریز
        runCatching {
            val f = File(filesDir, "last_crash.txt")
            if (f.exists()) {
                DebugLog.log("Crash", "═══ کرش اجرای قبلی ═══")
                f.readText().lines().take(80).forEach { DebugLog.log("Crash", it) }
                f.delete()
            }
        }

        // ── 2) نصب هندلر کرش برای اجراهای بعدی
        val original = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                File(filesDir, "last_crash.txt").writeText(sw.toString())
            }
            original?.uncaughtException(thread, throwable)
        }
    }
}