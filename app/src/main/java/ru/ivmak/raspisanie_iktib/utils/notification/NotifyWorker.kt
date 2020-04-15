package ru.ivmak.raspisanie_iktib.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.ivmak.raspisanie_iktib.R
import ru.ivmak.raspisanie_iktib.utils.Functions
import ru.ivmak.raspisanie_iktib.data.Table
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import ru.ivmak.raspisanie_iktib.ui.screens.main.MainActivity
import ru.ivmak.raspisanie_iktib.utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.DayOfWeek
import java.util.*

class NotifyWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {

    val TAG = Constants.WORKER_TAG
    lateinit var timeTable: TimeTable

    var timeTableRepository = TimeTableRepository()

    override fun doWork(): Result {

        timeTable = timeTableRepository.getTimeTableFromSP()

        if (timeTable.result == "no_entries" || timeTable.table == null) {
            return Result.success()
        }

        var dayOfWeek = Functions.isDayOfWeekOpen(timeTable.table!!)
        if(dayOfWeek == -1) {
            GlobalScope.launch {
                timeTable = timeTableRepository.getTimeTable(timeTable.table!!.group)
                withContext(Dispatchers.Main) {
                    if (timeTable.result != "no_entries" && timeTable.table != null) {
                        dayOfWeek = Functions.isDayOfWeekOpen(timeTable.table!!)
                        if (dayOfWeek == -1) {
                            showNotification("Ошибка", "Не удалось загрузить расписание на сегодня")
                        } else {
                            buildNotification(dayOfWeek)
                            restartWorker()
                        }
                    }
                }
            }
        } else{
            buildNotification(dayOfWeek)
            restartWorker()
        }
        return Result.success()
    }

    private fun restartWorker() {

        Functions.scheduleNotification(
            applicationContext,
            (24 * 60 * 60 * 1000).toLong()
        )

    }

    private fun buildNotification(dayOfWeek: Int) {
        if (dayOfWeek == 7) {
            showNotification("Отдыхайте!", "У вас сегодня нет пар)")
        }
        else if (dayOfWeek >= 0) {
            var pairCount = 0
            var firstPair = ""
            for (i in 7 downTo 1) {
                if (timeTable.table!!.table[dayOfWeek + 2][i] != "") {
                    firstPair = timeTable.table!!.table[1][i]
                    pairCount++
                }
            }

            when (pairCount) {
                0 -> showNotification("Отдыхайте!", "У вас сегодня нет пар)")
                1 -> showNotification("Cегодня $pairCount пара", "Первая пара - $firstPair.")
                2, 3, 4 -> showNotification("Cегодня $pairCount пары", "Первая пара - $firstPair.")
                else -> showNotification("Cегодня $pairCount пар", "Первая пара - $firstPair.")
            }
        }
    }

    private fun showNotification(title: String, text: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_today_black_48)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,
                R.drawable.ic_launcher
            ))
            .setAutoCancel(true)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(PendingIntent.getActivity(applicationContext,0,intent,0))

        notificationManager.notify(1, notification.build())
    }
}
