package com.zelspeno.edisontesttask.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zelspeno.edisontesttask.repositories.SteamSource
import com.zelspeno.edisontesttask.source.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainViewModel(
) : ViewModel() {

    private val _gamesList = MutableSharedFlow<List<Apps>?>()
    val gamesList = _gamesList.asSharedFlow()

    private val _newsForGame = MutableSharedFlow<List<News>?>()
    val newsForGame = _newsForGame.asSharedFlow()


    fun getNewsForGame(appID: Long, feeds: String = "steam_community_announcements") {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SteamApi::class.java)

        val response: Call<AppNewsJsonObject> = api.getNewsForGameByAppID(appID, feeds)

        response.enqueue(object: Callback<AppNewsJsonObject> {
            override fun onResponse(call: Call<AppNewsJsonObject>, response: Response<AppNewsJsonObject>) {
                viewModelScope.launch {
                    val news = response.body()?.appNews?.news
                    if (news == null || news.isEmpty()) {
                        _newsForGame.emit(null)
                    } else {
                        _newsForGame.emit(news.map {
                                newsItem -> newsItem.getNewsInFormat()
                        })
                    }
                }
            }

            override fun onFailure(call: Call<AppNewsJsonObject>, t: Throwable) {
                viewModelScope.launch {
                    _newsForGame.emit(null)
                }
            }
        })
    }

    fun getListApps() {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(SteamApi::class.java)

        val response: Call<AppsJsonObject> = api.getGamesList()

        response.enqueue(object: Callback<AppsJsonObject> {
            override fun onResponse(call: Call<AppsJsonObject>, response: Response<AppsJsonObject>) {
                viewModelScope.launch {
                    _gamesList.emit(response.body()!!.appList.apps)
                }
            }

            override fun onFailure(call: Call<AppsJsonObject>, t: Throwable) {
                viewModelScope.launch {
                    _gamesList.emit(null)
                }
            }
        })
    }
}
