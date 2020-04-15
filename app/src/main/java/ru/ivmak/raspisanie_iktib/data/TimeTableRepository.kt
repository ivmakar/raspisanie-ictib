package ru.ivmak.raspisanie_iktib.data

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivmak.raspisanie_iktib.utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class TimeTableRepository @Inject constructor(private var applicationContext: Context) {

    val sPref = applicationContext.getSharedPreferences(Constants.APP_PREF, AppCompatActivity.MODE_PRIVATE)

    suspend fun searchByQuery(query: String): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?query=$query")
            tTable.isOnline = true
            return tTable
        }
        return TimeTable(null, null, null, "no_entries", false)
    }

    suspend fun getTimeTable(group: String): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?group=$group")
            tTable.isOnline = true
            saveTextToSP(Gson().toJson(tTable))
            return tTable
        }
        return TimeTable(null, null, null, "no_entries", false)
    }

    suspend fun getTimeTableByWeek(group: String, week: Int): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?group=$group&week=$week")
            tTable.isOnline = true
            return tTable
        }
        return TimeTable(null, null, null, "no_entries", false)
    }


    fun verifyAvailableNetwork(): Boolean{
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo= connectivityManager.activeNetworkInfo
        val isConnect = networkInfo != null && networkInfo.isConnected

        return isConnect
    }


    fun parseJson(jsonStr: String): TimeTable = Gson().fromJson<TimeTable>(jsonStr, TimeTable::class.java)

    fun getTimeTableFromSP(): TimeTable? {
        val tTable =  parseJson(loadTextFromSP())
        tTable.isOnline = false
        return tTable
    }

    fun saveTextToSP(data: String) {
        val ed = sPref.edit()
        ed.putString(Constants.LAST_TT, data)
        ed.commit()
    }

    fun loadTextFromSP(): String {
        val savedText: String = sPref.getString(Constants.LAST_TT, "{\"result\": \"no_entries\"}")
        return savedText
    }

    private suspend fun getTimeTableFromAPI(params: String): TimeTable {
        val jsonStr = withContext(Dispatchers.IO) {
            httpRequest(
                URL("http://165.22.28.187/schedule-api/$params")
            )
        }
        return parseJson(jsonStr)
    }

    private fun httpRequest(url: URL): String {
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

}