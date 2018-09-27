package me.gavin.base

import android.annotation.SuppressLint
import android.app.Application
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import me.gavin.app.AppDatabase
import me.gavin.app.entity.Source
import me.gavin.util.CrashHandler
import okio.Okio


class App : Application() {

    companion object {
        lateinit var app: App
            private set
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        CrashHandler.init()

        initSource()
    }

    @SuppressLint("CheckResult")
    private fun initSource() {
        Flowable.just("test.json")
                .map { assets.open(it) }
                .map { Okio.buffer(Okio.source(it)).readUtf8() }
                .map { it.fromJson<List<Source>>()!! }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    AppDatabase.getInstance(this)
                            .sourceDao()
                            .insertAll(it)
                }, { it.printStackTrace() })
    }
}