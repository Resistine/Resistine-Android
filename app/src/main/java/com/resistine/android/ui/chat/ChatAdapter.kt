package com.resistine.android.ui.chat

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R
import com.resistine.android.databinding.ItemMessageBinding
import com.resistine.android.databinding.ItemTypingBinding
import io.noties.markwon.Markwon

class ChatAdapter(private var messages: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isTyping: Boolean = false
    private var markwon: Markwon? = null

    companion object {
        private const val VIEW_TYPE_MESSAGE = 1
        private const val VIEW_TYPE_TYPING = 2
    }

    class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)
    
    class TypingViewHolder(val binding: ItemTypingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun startAnimation() {
            animateDot(binding.dot1, 0)
            animateDot(binding.dot2, 200)
            animateDot(binding.dot3, 400)
        }

        private fun animateDot(view: View, delay: Long) {
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.7f, 1.3f, 0.7f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.7f, 1.3f, 0.7f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.5f, 1f, 0.5f)
            )
            animator.duration = 1000
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.startDelay = delay
            animator.start()
        }
    }

    fun updateMessages(newMessages: List<ChatMessage>, isTyping: Boolean) {
        this.messages = newMessages
        this.isTyping = isTyping
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isTyping && position == messages.size) {
            VIEW_TYPE_TYPING
        } else {
            VIEW_TYPE_MESSAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TYPING) {
            TypingViewHolder(ItemTypingBinding.inflate(inflater, parent, false))
        } else {
            MessageViewHolder(ItemMessageBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TypingViewHolder) {
            holder.startAnimation()
            return
        }

        val messageViewHolder = holder as MessageViewHolder
        val message = messages[position]
        
        // Initialize Markwon if needed
        if (markwon == null) {
            markwon = Markwon.create(holder.itemView.context)
        }

        val layoutParams = messageViewHolder.binding.textViewMessage.layoutParams as ViewGroup.MarginLayoutParams

        if (message.isUser) {
            // User message: Plain text is fine
            messageViewHolder.binding.textViewMessage.text = message.text
            messageViewHolder.binding.textViewMessage.setBackgroundResource(R.drawable.background_home2)
            messageViewHolder.binding.textViewMessage.setTextColor(Color.WHITE)
            layoutParams.marginStart = 100
            layoutParams.marginEnd = 0
            messageViewHolder.binding.root.gravity = Gravity.END
        } else {
            // Bot message: Render Markdown
            markwon?.setMarkdown(messageViewHolder.binding.textViewMessage, message.text)

            messageViewHolder.binding.textViewMessage.setBackgroundResource(R.drawable.background_home)
            val context = holder.itemView.context
            val isDarkMode = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            if (isDarkMode) {
                messageViewHolder.binding.textViewMessage.setTextColor(Color.WHITE)
            } else {
                messageViewHolder.binding.textViewMessage.setTextColor(Color.BLACK)
            }
            
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 100
            messageViewHolder.binding.root.gravity = Gravity.START
        }

        messageViewHolder.binding.textViewMessage.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return if (isTyping) messages.size + 1 else messages.size
    }
}
