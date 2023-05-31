package com.zelspeno.edisontesttask.repositories

import com.zelspeno.edisontesttask.source.Apps
import com.zelspeno.edisontesttask.source.News
import kotlinx.coroutines.flow.Flow

interface SteamSource {

    /** Get the list of [Apps] from Steam Api */
    suspend fun getGamesList(): Flow<List<Apps>>

    /** Get the list of [News] to current game by [appID].
     * Can set the optional [feeds] to get news from selected source (feed) */
    suspend fun getNewsForGameByAppID(appID: Long, feeds: String = "none"): Flow<List<News>>

}