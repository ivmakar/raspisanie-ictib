package ru.ivmak.raspisanie_iktib.utils.di

import dagger.Component
import dagger.android.AndroidInjector
import ru.ivmak.raspisanie_iktib.utils.App
import javax.inject.Singleton


@Singleton
@Component(
    modules = [AppModule::class])

interface AppComponent : AndroidInjector<App> {


    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}