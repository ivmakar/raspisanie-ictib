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

    var choices = MutableLiveData<ArrayList<Choice>>()


    suspend fun searchByQuery(query: String) {
        val tTable = getTimeTableFromAPI("?query=$query")
        withContext(Dispatchers.Main) {
            when {
                tTable.result != null -> choices.value = arrayListOf()
                tTable.choices != null -> choices.value = tTable.choices
                tTable.table != null -> choices.value =
                    arrayListOf(Choice(tTable.table!!.name, "", tTable.table!!.group))
            }
        }
    }

    suspend fun getTimeTable(group: String) {
        val tTable = getTimeTableFromAPI("?group=$group")
        withContext(Dispatchers.Main) {
            if (tTable.table != null) {
                timeTable.value = tTable
            }
        }
    }

    suspend fun getTimeTableByWeek(group: String, week: Int) {
        val tTable = getTimeTableFromAPI("?group=$group&week=$week")
        withContext(Dispatchers.Main) {
            if (tTable.table != null) {
                timeTable.value = tTable
            }
        }
    }

    private fun parseJson(jsonStr: String): TimeTable = Gson().fromJson<TimeTable>(jsonStr, TimeTable::class.java)

    private suspend fun getTimeTableFromAPI(params: String): TimeTable =
        parseJson(withContext(Dispatchers.IO){
            httpRequest(
                URL("http://165.22.28.187/schedule-api/$params")
            )
        })

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