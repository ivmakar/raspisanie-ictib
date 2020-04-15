package ru.ivmak.raspisanie_iktib.utils

import android.app.Application
import android.content.Context

class App : Application() {

    companion object{
        private lateinit var applicationContext: Context
        fun getApplicationContext() = App.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        App.applicationContext = applicationContext
    }
}