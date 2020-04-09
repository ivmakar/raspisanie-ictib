package ru.ivmak.raspisanie_iktib.data

import ru.ivmak.raspisanie_iktib.data.Choice
import ru.ivmak.raspisanie_iktib.data.Table

class TimeTable (
    var table: Table?,
    var weeks: ArrayList<Int>?,
    val choices: ArrayList<Choice>?,
    val result: String?
)