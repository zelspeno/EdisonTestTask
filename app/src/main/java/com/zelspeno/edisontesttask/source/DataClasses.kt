package com.zelspeno.edisontesttask.source

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AppsUI(
    val gameID: Long,
    val name: String,
    val image: String
): Serializable

data class News(
    val newsID: Long,
    val title: String,
    val datetime: Long,
    val body: String,
    val url: String
): Serializable

data class NewsUI(
    val newsID: Long,
    val title: String,
    val datetime: String,
    val body: String,
    val url: String,
    val headerPhoto: String
): Serializable

data class AppsJsonObject(
    @SerializedName("applist")
    val appList: AppList
)

data class AppList(
    @SerializedName("apps")
    val apps: List<Apps>
)

data class Apps(
    @SerializedName("appid")
    val gameID: Long,
    val name: String
): Serializable

data class AppNewsJsonObject(
    @SerializedName("appnews")
    val appNews: AppNews
)

data class AppNews(
    val appid: Long,
    @SerializedName("newsitems")
    val news: List<NewsItem>,
    val count: Int
)

data class NewsItem(
    val gid: Long,
    val title: String,
    val url: String,
    val is_external_url: Boolean,
    val author: String,
    val contents: String,
    @SerializedName("feedlabel") val feedLabel: String,
    val date: Long,
    @SerializedName("feedname") val feedName: String,
    @SerializedName("feed_type") val feedType: String,
    val appid: Long,
    val tags: List<String> = emptyList()
) {
    fun getNewsInFormat(): News = News(
        newsID = gid,
        title = title,
        datetime = date,
        body = contents,
        url = url
    )
}
