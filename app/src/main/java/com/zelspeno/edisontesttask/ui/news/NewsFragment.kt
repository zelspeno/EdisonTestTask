package com.zelspeno.edisontesttask.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.databinding.FragmentNewsBinding
import com.zelspeno.edisontesttask.source.*
import com.zelspeno.edisontesttask.ui.main.MainViewModel
import com.zelspeno.edisontesttask.utils.viewModelCreator
import kotlinx.coroutines.launch

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
            viewModel.moveToFragment(view, R.id.navigation_mainFragment)
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
                            val newsUI = viewModel.prepareNewsToUI(it, onErrorImage = game.image)
                            adapter = CustomNewsListRecyclerAdapter(newsUI)
                            sendDataToRecyclerView(view, adapter!!)
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
                            val newsUI = viewModel.prepareNewsToUI(it, onErrorImage = game.image)
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
    private fun sendDataToRecyclerView(v: View?, adapterRV: CustomNewsListRecyclerAdapter) {
        val recyclerView = binding.newsRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    (layoutManager as LinearLayoutManager).orientation
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
                viewModel.moveToFragment(v, R.id.navigation_moreFragment, bundle)
            }
        })
    }
}