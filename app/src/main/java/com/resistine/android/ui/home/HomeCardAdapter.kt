package com.resistine.android.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.databinding.ItemFragmentHomeBinding

class HomeCardAdapter(
    private val onItemClick: (HomeCardItem) -> Unit
) : ListAdapter<HomeCardItem, HomeCardAdapter.HomeCardViewHolder>(DiffCallback) {

    inner class HomeCardViewHolder(
        private val binding: ItemFragmentHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeCardItem) = with(binding) {
            imageViewFragmentIcon.setImageResource(item.iconResId)
            textViewFragmentTitle.text = item.title
            textViewFragmentSummary.text = item.summary
            textViewFragmentStatus.text = item.status
            textViewFragmentStatus.setTextColor(
                ContextCompat.getColor(root.context, item.statusColorResId)
            )
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCardViewHolder {
        val binding = ItemFragmentHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<HomeCardItem>() {
        override fun areItemsTheSame(old: HomeCardItem, new: HomeCardItem): Boolean =
            old.destinationFragmentId == new.destinationFragmentId

        override fun areContentsTheSame(old: HomeCardItem, new: HomeCardItem): Boolean = old == new
    }
}
