package com.zelspeno.edisontesttask.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.*
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.picasso.Picasso
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.databinding.FragmentMainBinding
import com.zelspeno.edisontesttask.source.Apps
import com.zelspeno.edisontesttask.source.AppsUI
import com.zelspeno.edisontesttask.source.Const
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

        swipeRefresher.setOnRefreshListener {
            binding.gamesRecyclerView.visibility = View.GONE
            binding.gamesRecyclerViewNotFound.visibility = View.INVISIBLE
            searchView.setQuery("", false)
            searchView.clearFocus()
            updateList()
            swipeRefresher.isRefreshing = false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                if (text?.length in 1..3) {
                    Toast.makeText(context, "Введите больше символов", Toast.LENGTH_SHORT).show()
                }
                else if (text?.length!! > 3) {
                    fillListWithSearch(text)
                    return false
                } else {
                    fillList()
                }
                return true
            }
        })

        fillList()

        return binding.root
    }

    private fun prepareDataToUI(list: List<Apps>): List<AppsUI> {
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

    private fun sendDataToRecyclerView(adapterRV: CustomGamesListRecyclerAdapter) {
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
                view?.findNavController()
                    ?.navigate(R.id.navigation_newsFragment, bundle)
            }
        })
    }

    private fun fillList() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getListApps()
                viewModel.gamesList.collect {
                    if (it != null) {
                        val gamesList = prepareDataToUI(it)
                        adapter = CustomGamesListRecyclerAdapter(gamesList)
                        sendDataToRecyclerView(adapter!!)
                        binding.gamesRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.gamesRecyclerViewNotFound.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun updateList() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getListApps()
                viewModel.gamesList.collect {
                    if (it != null) {
                        val gamesList = prepareDataToUI(it)
                        adapter?.updateList(gamesList)
                        binding.gamesRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.gamesRecyclerViewNotFound.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun fillListWithSearch(text: String?) {
        val initList: List<AppsUI>? = adapter?.getList()
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
        adapter?.updateList(resList)
    }

    private fun getHeaderImagePathByAppID(appid: Long): String =
        "${Const.STORAGE_URL}/apps/$appid/header.jpg"

}