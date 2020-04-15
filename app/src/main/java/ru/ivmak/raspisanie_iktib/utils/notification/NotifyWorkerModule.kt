package ru.ivmak.raspisanie_iktib.utils.notification

import dagger.Provides
import androidx.work.WorkerFactory
import dagger.Module
import ru.example.ivan.smssender.utility.di.DaggerWorkerFactory
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import javax.inject.Singleton

@Module
class NotifyWorkerModule {
    @Provides
    @Singleton
    fun workerFactory(timeTableRepository: TimeTableRepository): WorkerFactory {
        return DaggerWorkerFactory(timeTableRepository)
    }
}