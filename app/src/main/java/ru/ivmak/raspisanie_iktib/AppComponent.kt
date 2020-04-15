package ru.ivmak.raspisanie_iktib

import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import ru.ivmak.raspisanie_iktib.ui.screens.main.MainActivityModule
import ru.ivmak.raspisanie_iktib.utils.notification.NotifyWorkerModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [AndroidSupportInjectionModule::class,
        AppModule::class,
        MainActivityModule::class,
        NotifyWorkerModule::class])

interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}