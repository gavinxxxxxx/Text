package me.gavin.base

import android.app.Application
import me.gavin.util.CrashHandler


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
        CrashHandler.init()
    }

    companion object {
        lateinit var app: App
            private set
    }
}