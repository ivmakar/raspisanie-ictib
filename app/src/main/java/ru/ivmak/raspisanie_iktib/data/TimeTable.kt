package ru.ivmak.raspisanie_iktib.data

import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.Table
import ru.ivmak.raspisanie_iktib.utils.Constants

class TimeTable (
    var table: Table?,
    var weeks: ArrayList<Int>?,
    val choices: ArrayList<Choice>?,
    var result: String? = Constants.RESULT_OK
)