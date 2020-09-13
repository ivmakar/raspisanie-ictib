package ru.ivmak.raspisanie_iktib.ui.screens.main

import android.provider.SyncStateContract
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.TimeTable
import ru.ivmak.raspisanie_iktib.data.TimeTableRepository
import ru.ivmak.raspisanie_iktib.utils.Constants
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainViewModel @Inject constructor(val repository: TimeTableRepository) : ViewModel() {

//    companion object {
//        val _instance = MainDataSingleton()
//        fun getInstance() = _instance
//    }

    var timeTable = MutableLiveData<TimeTable>()

    var choices = MutableLiveData<ArrayList<Choice>>()

    suspend fun initTimeTable() {
        withContext(Dispatchers.Main) {
            timeTable.value = repository.getTimeTableFromSP()
        }
    }

    suspend fun searchByQuery(query: String) {
        val tTable = repository.searchByQuery(query)
        withContext(Dispatchers.Main) {
            timeTable.value?.result = tTable.result
            when {
                tTable.result != Constants.RESULT_OK -> choices.value = arrayListOf()
                tTable.choices != null -> choices.value = tTable.choices
                tTable.table != null -> choices.value =
                    arrayListOf(
                        Choice(
                            tTable.table!!.name,
                            "",
                            tTable.table!!.group
                        )
                    )
                else -> choices.value = arrayListOf()
            }
        }
    }

    suspend fun getTimeTable(group: String) {
        val tTable = repository.getTimeTable(group)
        withContext(Dispatchers.Main) {
                timeTable.value = tTable
        }
    }

    suspend fun getTimeTableByWeek(group: String, week: Int) {
        val tTable = repository.getTimeTableByWeek(group, week)
        withContext(Dispatchers.Main) {
                timeTable.value = tTable
        }
    }
}