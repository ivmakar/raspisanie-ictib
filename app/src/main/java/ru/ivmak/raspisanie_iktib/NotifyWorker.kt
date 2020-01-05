package ru.ivmak.raspisanie_iktib

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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

    val TAG = "NotifyWorker"
    lateinit var timeTable: TimeTable

    override fun doWork(): WorkerResult {

        val savedText: String = inputData.getString(Constants.LAST_TT, "{\"result\": \"no_entries\"}")

        timeTable = parseJson(savedText)

        if (timeTable.result == "no_entries" || timeTable.table == null) {
            return WorkerResult.SUCCESS
        }

        val dayOfWeek = isDayOfWeekOpen(timeTable.table!!)
        if (dayOfWeek == 7) {
            showNotification("У вас сегодня нет пар)")
        }
        else if (dayOfWeek >= 0) {
            var pairCount = 0
            var firstPair = ""
            for (i in 6 downTo 0) {
                if (timeTable.table!!.table[dayOfWeek + 2][i] != "") {
                    firstPair = timeTable.table!!.table[0][i]
                    pairCount++
                }
            }

            showNotification(if (pairCount == 0) "У вас сегодня нет пар)" else "У вас сегодня $pairCount пар. Первая пара - $firstPair.")
        }

        restartWorker()

        return WorkerResult.SUCCESS
    }

    private fun restartWorker() {

        val sPref = applicationContext.getSharedPreferences(Constants.APP_PREF, AppCompatActivity.MODE_PRIVATE)
        val data = Data.Builder()
            .putString(Constants.LAST_TT, sPref.getString(Constants.LAST_TT, "{\"result\": \"no_entries\"}"))
            .build()

        WorkManager.getInstance().cancelAllWorkByTag("NotifyWorker")
        val myWorkRequest = OneTimeWorkRequest.Builder(NotifyWorker::class.java)
            .setInitialDelay(24, TimeUnit.HOURS)
            .setInputData(data)
            .build()
        WorkManager.getInstance().enqueue(myWorkRequest)

    }

    private fun showNotification(str: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                str,
                NotificationManager.IMPORTANCE_DEFAULT)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, R.drawable.ic_launcher))
            .setContentTitle(str)

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
