package ru.ivmak.raspisanie_iktib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotifyWorker : Worker() {

    val TAG = Constants.WORKER_TAG
    lateinit var timeTable: TimeTable

    override fun doWork(): WorkerResult {

        val savedText: String = inputData.getString(Constants.LAST_TT, "{\"result\": \"no_entries\"}")

        timeTable = parseJson(savedText)

        if (timeTable.result == "no_entries" || timeTable.table == null) {
            return WorkerResult.SUCCESS
        }

        val dayOfWeek = isDayOfWeekOpen(timeTable.table!!)
        if (dayOfWeek == 7) {
            showNotification("Отдыхайте!", "У вас сегодня нет пар)")
        }
        else if (dayOfWeek >= 0) {
            var pairCount = 0
            var firstPair = ""
            for (i in 7 downTo 1) {
                if (timeTable.table!!.table[dayOfWeek + 2][i] != "") {
                    firstPair = timeTable.table!!.table[0][i]
                    pairCount++
                }
            }

            showNotification(if (pairCount == 0) "Отдыхайте!" else "Cегодня $pairCount пар", if (pairCount == 0) "У вас сегодня нет пар)" else "Первая пара - $firstPair.")
        } else if (dayOfWeek == -1) {
            showNotification("Ошибка", "Не удалось загрузить расписание")
        }

        restartWorker()

        return WorkerResult.SUCCESS
    }

    private fun restartWorker() {

        Utils.scheduleNotification(applicationContext, (24*60*60*1000).toLong())

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
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_launcher))
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(PendingIntent.getActivity(applicationContext,0,intent,0))

        notificationManager.notify(1, notification.build())

    }

    fun getTimeTable(group: String) {
        val tTable = getTimeTableFromAPI("?group=$group")
        if (tTable.table != null) {
            timeTable = tTable
        }
    }

    fun parseJson(jsonStr: String): TimeTable = Gson().fromJson<TimeTable>(jsonStr, TimeTable::class.java)

    fun getTimeTableFromAPI(params: String): TimeTable =
        parseJson(
            httpRequest(
                URL("http://165.22.28.187/schedule-api/$params")
            ))


    fun httpRequest(url: URL): String {
        val response = StringBuffer()
        with(url.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            BufferedReader(InputStreamReader(inputStream)).use {

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
            }
        }
        return response.toString()
    }

    fun isDayOfWeekOpen(table: Table): Int {
        val months = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
        val date = Date()
        if (date.day == 0) {
            return 7
        }
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
