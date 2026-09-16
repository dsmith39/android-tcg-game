package com.milehighweb.riftclash

import android.app.Application
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Writes any uncaught exception (including ones thrown deep inside Compose recomposition,
 * which no try/catch in [GameViewModel] can reach) to a plain file before the process dies,
 * so a crash can be diagnosed from the device alone -- no adb/logcat access required.
 * [MainActivity] reads this file back on the next launch and shows it as a dismissible report.
 */
object CrashReporter {

    const val CRASH_FILE_NAME = "last_crash.txt"

    fun install(app: Application) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val report = buildString {
                    appendLine(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    appendLine("Thread: ${thread.name}")
                    appendLine()
                    appendLine(Log.getStackTraceString(throwable))
                }
                File(app.filesDir, CRASH_FILE_NAME).writeText(report)
            } catch (loggingFailure: Exception) {
                Log.e("CrashReporter", "Failed to persist crash report", loggingFailure)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    /** Reads and clears the last saved crash report, if any. */
    fun consumeLastCrash(app: Application): String? {
        val file = File(app.filesDir, CRASH_FILE_NAME)
        if (!file.exists()) return null
        val text = file.readText()
        file.delete()
        return text
    }
}
