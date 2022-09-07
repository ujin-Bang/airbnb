package com.modoohealing.airbnb

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {
    @GET("/v3/9df228ae-aac4-4f0b-8f7e-b29fc38344e6")
    fun getHouseList(): Call<HouseDto>
}