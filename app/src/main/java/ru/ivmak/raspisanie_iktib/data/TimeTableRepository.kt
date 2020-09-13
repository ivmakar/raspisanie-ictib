package ru.ivmak.raspisanie_iktib.data

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivmak.raspisanie_iktib.utils.App
import ru.ivmak.raspisanie_iktib.utils.Constants
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class TimeTableRepository @Inject constructor(val applicationContext: Context) {

    val sPref = applicationContext.getSharedPreferences(Constants.APP_PREF, AppCompatActivity.MODE_PRIVATE)

    suspend fun searchByQuery(query: String): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?query=$query")
            if (tTable.result == null) {
                tTable.result = Constants.RESULT_OK
            }
            return tTable
        }
        return TimeTable(null, null, arrayListOf(), Constants.CONNECTION_FAIL)
    }

    suspend fun getTimeTable(group: String): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?group=$group")
            saveTextToSP(Gson().toJson(tTable))
            return returnServerError(tTable)
        }
        return returnConnectionFail()
    }

    suspend fun getTimeTableByWeek(group: String, week: Int): TimeTable {
        if (verifyAvailableNetwork()) {
            val tTable = getTimeTableFromAPI("?group=$group&week=$week")
            return returnServerError(tTable)
        }
        return returnConnectionFail()
    }


    fun verifyAvailableNetwork(): Boolean{
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo= connectivityManager.activeNetworkInfo
        val isConnect = networkInfo != null && networkInfo.isConnected

        return isConnect
    }

    private fun returnServerError(tTable: TimeTable): TimeTable {
        if (tTable.result == null) {
            tTable.result = Constants.RESULT_OK
        } else if (tTable.result == Constants.SERVER_ERROR && tTable.table == null) {
            val table = getTimeTableFromSP()
            tTable.table = table.table
            tTable.weeks = table.weeks
        }
        return tTable
    }

    private fun returnConnectionFail(): TimeTable {
        val tTable = getTimeTableFromSP()
        if (tTable.result != null) {
            return TimeTable(null, null, null, Constants.CONNECTION_FAIL)
        } else {
            return TimeTable(tTable.table, tTable.weeks, null, Constants.CONNECTION_FAIL)
        }
    }


    fun parseJson(jsonStr: String): TimeTable = Gson().fromJson<TimeTable>(jsonStr, TimeTable::class.java)

    fun getTimeTableFromSP(): TimeTable {
        val tTable =  parseJson(loadTextFromSP())
        return tTable
    }

    fun saveTextToSP(data: String) {
        val ed = sPref.edit()
        ed.putString(Constants.LAST_TT, data)
        ed.apply()
    }

    private fun loadTextFromSP(): String {
        val savedText: String? = sPref.getString(Constants.LAST_TT, "{\"result\": \"${Constants.SP_EMPTY}\"}")
        return savedText!!
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

            try {
                BufferedReader(InputStreamReader(inputStream)).use {

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                }
            } catch (e: FileNotFoundException) {
                response.append("{\"result\": \"${Constants.SERVER_ERROR}\"}")
            }
        }
        return response.toString()
    }

}