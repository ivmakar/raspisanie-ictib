package ru.ivmak.raspisanie_iktib.data

class Table (
    var type: String,
    var name: String,
    var week: Int,
    var group: String,
    var table: ArrayList<ArrayList<String>>
)