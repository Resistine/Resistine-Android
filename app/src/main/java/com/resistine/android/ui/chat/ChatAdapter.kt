package com.resistine.android.ui.chat

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R
import com.resistine.android.databinding.ItemMessageBinding
import com.resistine.android.databinding.ItemTypingBinding
import io.noties.markwon.Markwon

internal sealed interface ChatRow {
    data class Message(val value: ChatMessage) : ChatRow
    data object Typing : ChatRow
}

internal class ChatAdapter(
    private val markwon: Markwon,
    private val onCopy: (String) -> Unit
) : ListAdapter<ChatRow, RecyclerView.ViewHolder>(DiffCallback()) {

    fun submitState(state: ChatUiState) {
        val rows = state.messages.map(ChatRow::Message).toMutableList<ChatRow>()
        if (state.isTyping) rows += ChatRow.Typing
        submitList(rows)
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ChatRow.Message -> VIEW_TYPE_MESSAGE
        ChatRow.Typing -> VIEW_TYPE_TYPING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TYPING) {
            TypingViewHolder(ItemTypingBinding.inflate(inflater, parent, false))
        } else {
            MessageViewHolder(
                ItemMessageBinding.inflate(inflater, parent, false),
                markwon,
                onCopy
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is ChatRow.Message -> (holder as MessageViewHolder).bind(row.value)
            ChatRow.Typing -> (holder as TypingViewHolder).startAnimation()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is TypingViewHolder) holder.stopAnimation()
        super.onViewRecycled(holder)
    }

    private class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val markwon: Markwon,
        private val onCopy: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            val context = binding.root.context
            binding.root.gravity = if (message.isUser) Gravity.END else Gravity.START
            if (message.isUser) {
                binding.textViewMessage.text = message.text
                binding.textViewMessage.movementMethod = null
            } else {
                markwon.setMarkdown(binding.textViewMessage, message.text)
                binding.textViewMessage.movementMethod = LinkMovementMethod.getInstance()
            }
            binding.textViewMessage.setBackgroundResource(
                if (message.isUser) R.drawable.background_home2 else R.drawable.background_home
            )
            binding.textViewMessage.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (message.isUser) R.color.white else R.color.rs_text_primary
                )
            )
            binding.messageActions.visibility = if (message.isUser) View.GONE else View.VISIBLE
            binding.buttonCopyMessage.setOnClickListener { onCopy(message.text) }
            binding.textViewMessage.setOnLongClickListener {
                onCopy(message.text)
                true
            }
        }
    }

    private class TypingViewHolder(
        private val binding: ItemTypingBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val animators = listOf(
            createAnimator(binding.dot1, 0),
            createAnimator(binding.dot2, 160),
            createAnimator(binding.dot3, 320)
        )

        fun startAnimation() = animators.forEach { if (!it.isStarted) it.start() }
        fun stopAnimation() = animators.forEach(ObjectAnimator::cancel)

        private fun createAnimator(view: View, delay: Long): ObjectAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.75f, 1.25f, 0.75f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.75f, 1.25f, 0.75f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.45f, 1f, 0.45f)
            ).apply {
                duration = 900
                repeatCount = ObjectAnimator.INFINITE
                startDelay = delay
            }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatRow>() {
        override fun areItemsTheSame(oldItem: ChatRow, newItem: ChatRow): Boolean = when {
            oldItem is ChatRow.Message && newItem is ChatRow.Message -> oldItem.value.id == newItem.value.id
            oldItem === ChatRow.Typing && newItem === ChatRow.Typing -> true
            else -> false
        }

        override fun areContentsTheSame(oldItem: ChatRow, newItem: ChatRow): Boolean = oldItem == newItem
    }

    private companion object {
        const val VIEW_TYPE_MESSAGE = 1
        const val VIEW_TYPE_TYPING = 2
    }
}
