package ru.ivmak.raspisanie_iktib.utils

import android.app.Application
import android.content.Context
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class App : DaggerApplication() {

    companion object{
        private lateinit var applicationContext: Context
        fun getApplicationContext() = App.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        App.applicationContext = applicationContext
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }
}