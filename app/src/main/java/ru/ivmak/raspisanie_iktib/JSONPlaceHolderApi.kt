package ru.ivmak.raspisanie_iktib

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface JSONPlaceHolderApi {
    @GET("schedule-api/")
    fun getTimeTable(@Query("group") group: String): TimeTable

    @GET("schedule-api/")
    fun getTimeTableByWeek(@Query("group") group: String, @Query("week") week: Int): TimeTable
}