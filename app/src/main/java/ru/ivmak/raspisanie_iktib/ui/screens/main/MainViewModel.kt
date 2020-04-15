package ru.ivmak.raspisanie_iktib.ui.screens.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.Table
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainViewModel @Inject constructor(private var repository: TimeTableRepository) : ViewModel() {

    var timeTable = MutableLiveData<TimeTable>()

    var choices = MutableLiveData<ArrayList<Choice>>()
    
    var isConnection = false


    suspend fun initTimeTable() {
        withContext(Dispatchers.Main) {
            timeTable.value = repository.getTimeTableFromSP()
        }
    }

    suspend fun searchByQuery(query: String) {
        val tTable = repository.searchByQuery(query)
        withContext(Dispatchers.Main) {
            when {
                tTable.result != null -> choices.value = arrayListOf()
                tTable.choices != null -> choices.value = tTable.choices
                tTable.table != null -> choices.value =
                    arrayListOf(
                        Choice(
                            tTable.table!!.name,
                            "",
                            tTable.table!!.group
                        )
                    )
            }
        }
    }

    suspend fun getTimeTable(group: String) {
        val tTable = repository.getTimeTable(group)
        withContext(Dispatchers.Main) {
            if (tTable.table != null) {
                timeTable.value = tTable
            }
        }
    }

    suspend fun getTimeTableByWeek(group: String, week: Int) {
        val tTable = repository.getTimeTableByWeek(group, week)
        withContext(Dispatchers.Main) {
            if (tTable.table != null) {
                timeTable.value = tTable
            }
        }
    }
}