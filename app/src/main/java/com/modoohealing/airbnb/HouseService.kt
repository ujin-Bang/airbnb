package com.modoohealing.airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/61767330-4c56-4bc0-943b-c060f6767395")
    fun getHouseList(): Call<HouseDto>
}