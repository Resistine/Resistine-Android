package com.resistine.android.ui.login

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class OtpEditText : AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var onPasteListener: ((String) -> Unit)? = null

    fun setOnPasteListener(listener: (String) -> Unit) {
        this.onPasteListener = listener
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if (id == android.R.id.paste) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val pastedText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
            if (pastedText != null) {
                onPasteListener?.invoke(pastedText)
                // We return true to indicate that we have handled the paste event.
                // The default behavior (pasting into this EditText) is suppressed.
                return true
            }
        }
        return super.onTextContextMenuItem(id)
    }
}
