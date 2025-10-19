// HomeCardAdapter.kt
package com.resistine.android.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R

class HomeCardAdapter(
    private val items: List<HomeCardItem>,
    private val onItemClick: (HomeCardItem) -> Unit
) : RecyclerView.Adapter<HomeCardAdapter.HomeCardViewHolder>() {

    inner class HomeCardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon = view.findViewById<ImageView>(R.id.imageView_fragment_icon)
        private val title = view.findViewById<TextView>(R.id.textView_fragment_title)
        private val summary = view.findViewById<TextView>(R.id.textView_fragment_summary)
        private val status = view.findViewById<TextView>(R.id.textView_fragment_status)

        fun bind(item: HomeCardItem) {
            icon.setImageResource(item.iconResId)
            title.text = item.title
            summary.text = item.summary
            status.text = item.status

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fragment_home, parent, false)
        return HomeCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeCardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
