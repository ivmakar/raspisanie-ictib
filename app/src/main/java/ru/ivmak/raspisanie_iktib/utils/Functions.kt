package ru.ivmak.raspisanie_iktib.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import ru.ivmak.raspisanie_iktib.data.Table
import ru.ivmak.raspisanie_iktib.utils.notification.NotifyWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Functions {

    companion object {
        fun scheduleNotification(context: Context, duration: Long) {
            val sPref = context.getSharedPreferences(Constants.APP_PREF, AppCompatActivity.MODE_PRIVATE)
            val data = Data.Builder()
                .putString(
                    Constants.LAST_TT, sPref.getString(
                        Constants.LAST_TT, "{\"result\": \"no_entries\"}"))
                .build()

            WorkManager.getInstance().cancelAllWorkByTag(Constants.WORKER_TAG)
            val myWorkRequest = OneTimeWorkRequest.Builder(NotifyWorker::class.java)
                .setInitialDelay(duration, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(Constants.WORKER_TAG)
                .build()
            WorkManager.getInstance().enqueue(myWorkRequest)
        }

        fun getDuration(time: String) : Long {
            val formatter = SimpleDateFormat(Constants.TIME_FORMAT)

            val date = formatter.parse(time)
            val curDate = Date()

            val curDateLong = ((curDate.hours*60*60 + curDate.minutes*60 + curDate.seconds)*1000).toLong()
            var duration = ((date.hours*60*60 + date.minutes*60 + date.seconds)*1000).toLong()

            duration -= curDateLong
            if (duration <= 0) {
                duration += (24*60*60*1000).toLong()
            }

            return duration
        }

        fun isDayOfWeekOpen(table: Table): Int {
            val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
            val date = Date()
            for (i in 0..5) {
                var str = table.table[i + 2][0]
                val dayRegex = Regex("""([А-Я][а-я][а-я]),([0-9]+)\s+([а-я]+)""")
                val arr = dayRegex.findAll(str, 0)
                val (day, num, month) = arr.toList()[0].destructured
                if (Integer.parseInt(num) == date.date) {
                    if (months[date.month] == month) {
                        return i
                    }
                }
            }
            return -1
        }
    }
}