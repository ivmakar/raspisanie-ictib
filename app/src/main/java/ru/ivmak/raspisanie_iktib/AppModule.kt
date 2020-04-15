package ru.ivmak.raspisanie_iktib

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import javax.inject.Inject

@Module
class AppModule {

    @Provides
    fun providesContext(application: App) : Context {
        return application.applicationContext
    }
}