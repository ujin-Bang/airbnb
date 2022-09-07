package com.modoohealing.airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/dacd8f5a-f65d-4a16-a9b8-bf1a0b2423b2")
    fun getHouseList(): Call<HouseDto>
}