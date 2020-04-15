package ru.example.ivan.smssender.utility.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import ru.ivmak.raspisanie_iktib.utils.notification.NotifyWorker

class DaggerWorkerFactory(private val timeTableRepository: TimeTableRepository) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {

        val workerKlass = Class.forName(workerClassName).asSubclass(Worker::class.java)
        val constructor = workerKlass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        val instance = constructor.newInstance(appContext, workerParameters)

        when (instance) {
            is NotifyWorker -> {
                instance.timeTableRepository = timeTableRepository
            }
            // optionally, handle other workers               
        }

        return instance
    }
}