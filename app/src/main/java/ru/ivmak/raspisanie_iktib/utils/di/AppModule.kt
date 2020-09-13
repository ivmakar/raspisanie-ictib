package ru.ivmak.raspisanie_iktib.utils.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.ivmak.raspisanie_iktib.utils.App

@Module
class AppModule {

    @Provides
    fun providesContext(application: App) : Context {
        return application.applicationContext
    }
}