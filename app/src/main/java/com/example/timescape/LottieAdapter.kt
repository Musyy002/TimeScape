package com.example.timescape

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

class LottieAdapter(private val animations: List<Int>, private val titles: List<Int>, private val descriptions: List<Int>) :
    RecyclerView.Adapter<LottieAdapter.LottieViewHolder>() {

    class LottieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lottieAnimationView: LottieAnimationView = itemView.findViewById(R.id.lottieAnimationView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LottieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lottie, parent, false)
        return LottieViewHolder(view)
    }

    override fun onBindViewHolder(holder: LottieViewHolder, position: Int) {
        try {
            holder.lottieAnimationView.setAnimation(animations[position])
            holder.titleTextView.setText(titles[position])
            holder.descriptionTextView.setText(descriptions[position])
        } catch (e: Exception) {
            Log.e("LottieAdapter", "Error setting animation", e)
        }
    }

    override fun getItemCount(): Int {
        return animations.size
    }
}

