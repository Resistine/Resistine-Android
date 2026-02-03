package com.resistine.android.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.resistine.android.R
import com.resistine.android.security.WazuhAgent

class LogViewerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_log_viewer, container, false)
        val logTextView = view.findViewById<TextView>(R.id.logTextView)

        val wazuhAgent = WazuhAgent.getInstance(requireContext())
        logTextView.text = wazuhAgent.readLogs()

        return view
    }
}
