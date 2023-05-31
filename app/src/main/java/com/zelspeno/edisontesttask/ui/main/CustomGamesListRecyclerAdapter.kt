package com.zelspeno.edisontesttask.ui.main

import android.graphics.drawable.Drawable
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.source.Apps
import com.zelspeno.edisontesttask.source.AppsUI

class CustomGamesListRecyclerAdapter(private var games: List<AppsUI>):
    RecyclerView.Adapter<CustomGamesListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(game: AppsUI)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.gamesRVName)
        val image: ImageView = itemView.findViewById(R.id.gamesRVImage)
        lateinit var game: AppsUI

        init {
            itemView.setOnClickListener {
                listener.onItemClick(game)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.games_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = games[position].name
        Picasso.get().load(games[position].image)
            .error(R.drawable.steam_header)
            .into(holder.image)

        holder.game = games[position]
    }

    override fun getItemCount(): Int = games.size

    fun updateList(list: List<AppsUI>) {
        this.games = list
        notifyDataSetChanged()
    }

    fun getList(): List<AppsUI> = this.games

}