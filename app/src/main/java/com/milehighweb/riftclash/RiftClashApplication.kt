package com.milehighweb.riftclash

import android.app.Application

class RiftClashApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashReporter.install(this)
    }
}
