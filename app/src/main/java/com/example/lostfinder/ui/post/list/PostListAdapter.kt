package com.example.lostfinder.ui.post.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lostfinder.R
import com.example.lostfinder.data.model.post.PostListItem

class PostListAdapter(
    private val items: List<PostListItem>,
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imagePost)
        val title: TextView = view.findViewById(R.id.textTitle)
        val time: TextView = view.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        holder.title.text = post.title
        holder.time.text = post.createdAt

        Glide.with(holder.itemView.context)
            .load(post.imageUrl)
            .into(holder.img)

        holder.itemView.setOnClickListener {
            onClick(post.id)
        }
    }
}
