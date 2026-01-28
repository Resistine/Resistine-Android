package com.resistine.android.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R
import com.resistine.android.databinding.ItemMessageBinding

class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        holder.binding.textViewMessage.text = message.text

        val layoutParams = holder.binding.textViewMessage.layoutParams as ViewGroup.MarginLayoutParams

        if (message.isUser) {
            // User message: keep the original style
            holder.binding.textViewMessage.setBackgroundResource(R.drawable.background_home2)
            layoutParams.marginStart = 200
            layoutParams.marginEnd = 0
            holder.binding.root.gravity = Gravity.END
        } else {
            // Bot message: full width, no bubble
            holder.binding.textViewMessage.background = null // Remove the bubble
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 0 // Make it full width
            holder.binding.root.gravity = Gravity.START
        }

        holder.binding.textViewMessage.layoutParams = layoutParams
    }

    override fun getItemCount(): Int = messages.size

}
