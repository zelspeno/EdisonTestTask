package com.zelspeno.edisontesttask.ui.main

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.zelspeno.edisontesttask.source.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel : ViewModel() {

    private val _gamesList = MutableSharedFlow<List<Apps>?>()
    val gamesList = _gamesList.asSharedFlow()

    private val _newsForGame = MutableSharedFlow<List<News>?>()
    val newsForGame = _newsForGame.asSharedFlow()

    /** Get data for [newsForGame].
     * OnSuccess - return List<[News]>;
     * OnFail - return null */
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

    /** Get data for [gamesList].
     * OnSuccess - return List<[Apps]>;
     * OnFail - return null */
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

    /** Get List<[Apps]> from storage then convert it to display's List<[AppsUI]> */
    fun prepareAppsToUI(list: List<Apps>): List<AppsUI> {
        val result = mutableListOf<AppsUI>()
        for (i in list) {
            if (i.name.isNotEmpty()) {
                result.add(
                    AppsUI(
                        gameID = i.gameID,
                        name = i.name,
                        image = getHeaderImagePathByAppID(i.gameID)
                    )
                )
            }
        }
        return result
    }

    /** Get [data] from storage then convert it to display's List<[NewsUI]> */
    fun prepareNewsToUI(data: List<News>, onErrorImage: String): List<NewsUI> {
        val res = mutableListOf<NewsUI>()
        for (i in data) {
            res.add(
                NewsUI(
                    newsID = i.newsID,
                    title = i.title,
                    datetime = getUIDateTimeFromUnix(i.datetime),
                    body = migrateTextToHtml(i.body),
                    url = i.url,
                    headerPhoto = getHeaderPhoto(i.body, onErrorImage)
                )
            )
        }
        return res
    }

    /** Get main (Header) image of App by it's [appid] */
    private fun getHeaderImagePathByAppID(appid: Long): String =
        "${Const.STORAGE_URL}/apps/$appid/header.jpg"

    /** Search in [initList] on [text] and return List of matches like [AppsUI]  */
     fun getListWithSearch(initList: List<AppsUI>?, text: String?): List<AppsUI> {
        val resList = mutableListOf<AppsUI>()
        if (initList != null) {
            for (i in initList) {
                if (i.name.lowercase().contains(text?.lowercase().toString())) {
                    if (i !in resList) {
                        resList.add(i)
                    }
                }
            }
        }
        return resList
    }

    /** Move to Fragment by [resID] */
    fun moveToFragment(v: View?, resID: Int, bundle: Bundle? = null) {
        if (bundle == null) {
            v?.findNavController()?.navigate(resID)
        } else {
            v?.findNavController()?.navigate(resID, bundle)
        }
    }

    /** Get unix-type datetime (ex.1672531200) and convert it
     * to Human-type datetime(ex. 1 января 2023, 00:00:00) */
    private fun getUIDateTimeFromUnix(unix: Long): String {
        val sdf = SimpleDateFormat(
            "dd MMMM yyyy, HH:mm:ss",
            Locale.forLanguageTag("ru-RU")
        )
        return sdf.format(unix * 1000)
    }

    /** Get news's path photo and return path, where it could be downloaded
     * or return path of game's header photo ([onErrorImage]) */
    private fun getHeaderPhoto(text: String, onErrorImage: String): String {
        var res = ""
        val path = text.substringAfter("[img]{STEAM_CLAN_IMAGE}","").substringBefore("[/img]","")
        res = if (path.isNotEmpty()) {
            "https://clan.akamai.steamstatic.com/images$path"
        } else {
            onErrorImage
        }
        return res
    }

    /** Get [text] from request then convert it to HTML-type text and return */
    private fun migrateTextToHtml(text: String): String {
        var res = text
            .replace("{STEAM_CLAN_IMAGE}", "https://clan.akamai.steamstatic.com/images")
            .replace("[img]", """<img src="""")
            .replace("[/img]", """"><br>""")

        for (i in 1..6) {
            res = res
                .replace("[h$i]", "<h$i>")
                .replace("[/h$i]", "</h$i>")
        }

        res = res
            .replaceFirst("[*]", "<li>")
            .replace("[*]", "</li>\n<li>")
            .replace("[list]", "<ul>")
            .replace("[/list]", "</li>\n</ul>")

        res = res
            .replace("[url=", "<a href=")
            .replace("[/url]", "</a>")
            .replace("]", ">")
            .replace("[", "<")

        return res
    }
}
