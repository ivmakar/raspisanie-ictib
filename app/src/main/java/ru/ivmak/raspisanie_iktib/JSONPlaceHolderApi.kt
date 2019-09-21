package ru.ivmak.raspisanie_iktib

import retrofit2.http.GET
import retrofit2.http.Path


interface JSONPlaceHolderApi {
    @GET("?query={group}")
    fun getTimeTable(@Path("group") group: String): TimeTable

    @GET("?group={group}&week={week}")
    fun getTimeTableByWeek(@Path("group") group: String, @Path("week") week: Int): TimeTable
}