package com.zelspeno.edisontesttask.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.databinding.FragmentNewsBinding
import com.zelspeno.edisontesttask.source.*
import com.zelspeno.edisontesttask.ui.main.MainViewModel
import com.zelspeno.edisontesttask.utils.viewModelCreator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NewsFragment : Fragment() {

    private val viewModel by viewModelCreator { MainViewModel() }

    private lateinit var binding: FragmentNewsBinding

    private var adapter: CustomNewsListRecyclerAdapter? = null

    private lateinit var game: AppsUI

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        game = this.arguments?.getSerializable("game") as AppsUI

        binding = FragmentNewsBinding.inflate(inflater, container, false)

        val swipeRefresher = binding.newsSwipeRefreshLayout

        fillList()

        swipeRefresher.setOnRefreshListener {
            onSwipeUpdateList()
        }

        binding.newsGameName.text = game.name

        binding.newsBackButton.setOnClickListener {
            moveToMainFragment(view)
        }

        return binding.root
    }

    /** Init(1st) fill RecyclerView */
    private fun fillList() {

        with(binding) {
            newsShimmerRecyclerView.visibility = View.VISIBLE
            newsRecyclerView.visibility = View.GONE
            newsNotFound.visibility = View.GONE
            newsShimmerRecyclerView.startShimmer()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getNewsForGame(game.gameID)
                viewModel.newsForGame.collect {
                    with(binding) {
                        if (it != null) {
                            val newsUI = prepareDataToUI(it)
                            adapter = CustomNewsListRecyclerAdapter(newsUI)
                            sendDataToRecyclerView(adapter!!)
                            newsShimmerRecyclerView.stopShimmer()
                            newsShimmerRecyclerView.visibility = View.GONE
                            newsRecyclerView.visibility = View.VISIBLE
                            newsNotFound.visibility = View.GONE
                        } else {
                            newsShimmerRecyclerView.stopShimmer()
                            newsShimmerRecyclerView.visibility = View.GONE
                            newsRecyclerView.visibility = View.GONE
                            newsNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /** Update data on RecyclerView when user pull-to-refresh */
    private fun onSwipeUpdateList() {
        with(binding) {
            newsRecyclerView.visibility = View.GONE
            newsNotFound.visibility = View.GONE
            newsShimmerRecyclerView.visibility = View.VISIBLE
            newsShimmerRecyclerView.startShimmer()
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.getNewsForGame(game.gameID)
                    viewModel.newsForGame.collect {
                        if (it != null) {
                            val newsUI = prepareDataToUI(it)
                            adapter?.updateList(newsUI)
                            newsSwipeRefreshLayout.isRefreshing = false
                            newsShimmerRecyclerView.stopShimmer()
                            newsShimmerRecyclerView.visibility = View.GONE
                            newsRecyclerView.visibility = View.VISIBLE
                        } else {
                            newsSwipeRefreshLayout.isRefreshing = false
                            newsShimmerRecyclerView.stopShimmer()
                            newsShimmerRecyclerView.visibility = View.GONE
                            newsNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /** Init settings for RecyclerView */
    private fun sendDataToRecyclerView(adapterRV: CustomNewsListRecyclerAdapter) {
        val recyclerView = binding.newsRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    activity,
                    DividerItemDecoration.HORIZONTAL
                )
            )
        }
        adapterRV.setOnItemClickListener(object :
            CustomNewsListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(news: NewsUI) {
                val bundle =
                    Bundle().apply {
                        putSerializable("news", news)
                        putSerializable("game", game)
                    }
                view?.findNavController()
                    ?.navigate(R.id.navigation_moreFragment, bundle)
            }
        })
    }

    /** Move to MainFragment */
    private fun moveToMainFragment(v: View?) {
        v?.findNavController()?.navigate(R.id.navigation_mainFragment)
    }

    /** Get [data] from storage then convert it to display's List<[NewsUI]> */
    private fun prepareDataToUI(data: List<News>): List<NewsUI> {
        val res = mutableListOf<NewsUI>()
        for (i in data) {
            res.add(
                NewsUI(
                    newsID = i.newsID,
                    title = i.title,
                    datetime = getUIDateTimeFromUnix(i.datetime),
                    body = migrateTextToHtml(i.body),
                    url = i.url,
                    headerPhoto = getHeaderPhoto(i.body)
                )
            )
        }
        return res
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
     * or return path of game's header photo */

    private fun getHeaderPhoto(text: String): String {
        var res = ""
        val path = text.substringAfter("[img]{STEAM_CLAN_IMAGE}","").substringBefore("[/img]","")
        res = if (path.isNotEmpty()) {
            "https://clan.akamai.steamstatic.com/images$path"
        } else {
            game.image
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



