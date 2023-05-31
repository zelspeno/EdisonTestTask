package com.zelspeno.edisontesttask.ui.news

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.zelspeno.edisontesttask.R
import com.zelspeno.edisontesttask.source.News
import com.zelspeno.edisontesttask.source.NewsUI

class CustomNewsListRecyclerAdapter(private var news: List<NewsUI>):
    RecyclerView.Adapter<CustomNewsListRecyclerAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(news: NewsUI)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    class MyViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val name: TextView = itemView.findViewById(R.id.newsRVName)
        val date: TextView = itemView.findViewById(R.id.newsRVDate)
        val body: TextView = itemView.findViewById(R.id.newsRVBody)
        val headerPhoto: ImageView = itemView.findViewById(R.id.newsRVImage)
        lateinit var news: NewsUI

        init {
            itemView.setOnClickListener {
                listener.onItemClick(news)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.news_recyclerview_item, parent, false
        )
        return MyViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = news[position].title
        holder.date.text = news[position].datetime
        holder.body.text = Html.fromHtml(news[position].body, Html.FROM_HTML_MODE_LEGACY)
        Picasso.get().load(news[position].headerPhoto)
            .error(R.drawable.steam_header)
            .into(holder.headerPhoto)
        holder.news = news[position]
    }

    override fun getItemCount(): Int = news.size

    fun updateList(list: List<NewsUI>) {
        this.news = list
        notifyDataSetChanged()
    }

    fun getList(): List<NewsUI> = this.news

}