package com.zelspeno.edisontesttask.source

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SteamApi {

    @GET("ISteamApps/GetAppList/v2")
    fun getGamesList(): Call<AppsJsonObject>

    @GET("ISteamNews/GetNewsForApp/v2")
    fun getNewsForGameByAppID(
        @Query("appid") appID: Long,
        @Query("feeds") feeds: String = "none"
    ): Call<AppNewsJsonObject>

    @GET("apps/{appId}/header.jpg")
    fun getGameImage(
        @Path("appId") appID: Long,
    ): Call<String>

}