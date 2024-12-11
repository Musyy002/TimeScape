package com.example.timescape

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.timescape.R

class VideoAdapter(
    private val videos: List<Uri>
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView: VideoView = itemView.findViewById(R.id.videoView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoUri = videos[position]
        holder.videoView.setVideoURI(videoUri)
        holder.videoView.seekTo(1) // Load the first frame
        holder.videoView.setOnClickListener {
            holder.videoView.start() // Play video on click
        }

    }

    override fun getItemCount(): Int {
        return videos.size
    }
}
