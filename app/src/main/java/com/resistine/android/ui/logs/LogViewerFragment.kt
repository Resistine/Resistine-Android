package com.resistine.android.ui.logs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.resistine.android.R
import com.resistine.android.security.WazuhAgent

/**
 * Fragment for viewing local Wazuh agent audit logs.
 */
class LogViewerFragment : Fragment() {

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The inflated View.
     */
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
