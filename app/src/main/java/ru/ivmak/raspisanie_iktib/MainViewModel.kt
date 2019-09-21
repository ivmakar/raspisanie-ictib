package ru.ivmak.raspisanie_iktib

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

    var timeTable = MutableLiveData<TimeTable>()



    private fun parseJson(jsonStr: String): TimeTable {
        val gson = Gson()
        return gson.fromJson<TimeTable>(jsonStr, TimeTable::class.java)
    }

    suspend fun getTimeTable(group: String): TimeTable {
        val url = URL("http://165.22.28.187/schedule-api/?query=$group")

        return parseJson(withContext(Dispatchers.IO){ httpRequest(url) })
    }

    suspend fun getTimeTableByWeek(group: String, week: Int): TimeTable {
        val url = URL("http://165.22.28.187/schedule-api/?group=$group&week=$week")

        return parseJson(withContext(Dispatchers.IO){ httpRequest(url) })
    }

    private fun httpRequest(url: URL): String {
        val response = StringBuffer()
        with(url.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "GET"

            println("URL : $url")
            println("Response Code : $responseCode")

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