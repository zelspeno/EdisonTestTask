package com.zelspeno.edisontesttask.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.databinding.FragmentMainBinding
import com.zelspeno.edisontesttask.source.AppsUI
import com.zelspeno.edisontesttask.utils.viewModelCreator
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private val viewModel by viewModelCreator { MainViewModel() }

    private lateinit var binding: FragmentMainBinding

    private var adapter: CustomGamesListRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater, container, false)

        val searchView = binding.mainSearchView
        val swipeRefresher = binding.mainSwipeRefreshLayout

        fillList()

        swipeRefresher.setOnRefreshListener {
            onSwipeUpdateList()
        }

        initSearchViewSettings(searchView)

        return binding.root
    }

    /** Init settings for RecyclerView */
    private fun sendDataToRecyclerView(v: View?, adapterRV: CustomGamesListRecyclerAdapter) {
        val recyclerView = binding.gamesRecyclerView
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
            CustomGamesListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(game: AppsUI) {
                val bundle =
                    Bundle().apply { putSerializable("game", game) }
                viewModel.moveToFragment(v, R.id.navigation_newsFragment, bundle)
            }
        })
    }

    /** Init(1st) fill RecyclerView */
    private fun fillList() {

        with(binding) {
            gamesShimmerRecyclerView.visibility = View.VISIBLE
            gamesRecyclerView.visibility = View.GONE
            gamesRecyclerViewNotFound.visibility = View.GONE
            gamesShimmerRecyclerView.startShimmer()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                with(binding) {
                    viewModel.getListApps()
                    viewModel.gamesList.collect {
                        if (it != null) {
                            val gamesList = viewModel.prepareAppsToUI(it)
                            adapter = CustomGamesListRecyclerAdapter(gamesList)
                            sendDataToRecyclerView(view, adapter!!)
                            gamesShimmerRecyclerView.stopShimmer()
                            gamesShimmerRecyclerView.visibility = View.GONE
                            gamesRecyclerView.visibility = View.VISIBLE
                            gamesRecyclerViewNotFound.visibility = View.GONE
                        } else {
                            gamesShimmerRecyclerView.stopShimmer()
                            gamesShimmerRecyclerView.visibility = View.GONE
                            gamesRecyclerView.visibility = View.GONE
                            gamesRecyclerViewNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /** Update data on RecyclerView when user pull-to-refresh */
    private fun onSwipeUpdateList() {
        with(binding) {
            gamesRecyclerView.visibility = View.GONE
            gamesRecyclerViewNotFound.visibility = View.GONE
            gamesShimmerRecyclerView.visibility = View.VISIBLE
            gamesShimmerRecyclerView.startShimmer()
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mainSearchView.setQuery("", false)
                    mainSearchPrompt.clearFocus()
                    viewModel.getListApps()
                    viewModel.gamesList.collect {
                        if (it != null) {
                            val gamesList = viewModel.prepareAppsToUI(it)
                            adapter?.updateList(gamesList)
                            mainSwipeRefreshLayout.isRefreshing = false
                            gamesShimmerRecyclerView.stopShimmer()
                            gamesShimmerRecyclerView.visibility = View.GONE
                            gamesRecyclerView.visibility = View.VISIBLE
                        } else {
                            mainSwipeRefreshLayout.isRefreshing = false
                            gamesShimmerRecyclerView.stopShimmer()
                            gamesShimmerRecyclerView.visibility = View.GONE
                            gamesRecyclerViewNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    /** Update data on RecyclerView when user clear [searchView] */
    private fun onClearSearchViewFillList(searchView: SearchView) {
        binding.mainSearchPrompt.visibility = View.INVISIBLE
        binding.gamesRecyclerView.visibility = View.GONE
        binding.gamesRecyclerViewNotFound.visibility = View.GONE
        binding.gamesShimmerRecyclerView.visibility = View.VISIBLE
        binding.gamesShimmerRecyclerView.startShimmer()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchView.setQuery("", false)
                searchView.clearFocus()
                viewModel.getListApps()
                viewModel.gamesList.collect {
                    if (it != null) {
                        val gamesList = viewModel.prepareAppsToUI(it)
                        adapter?.updateList(gamesList)
                        binding.gamesShimmerRecyclerView.stopShimmer()
                        binding.gamesShimmerRecyclerView.visibility = View.GONE
                        binding.gamesRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.gamesShimmerRecyclerView.stopShimmer()
                        binding.gamesShimmerRecyclerView.visibility = View.GONE
                        binding.gamesRecyclerViewNotFound.visibility = View.VISIBLE
                    }
                }

            }
        }
    }

    /** Init settings for [searchView] */
    private fun initSearchViewSettings(searchView: SearchView) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if (text?.length in 1..3) {
                    binding.mainSearchPrompt.visibility = View.VISIBLE
                }
                else if (text?.length!! > 3) {
                    binding.mainSearchPrompt.visibility = View.INVISIBLE
                    val initList = adapter?.getList()
                    val searchList = viewModel.getListWithSearch(initList, text)
                    adapter?.updateList(searchList)
                    return false
                } else {
                    onClearSearchViewFillList(searchView)
                }
                return true
            }

        })
    }
}