package com.resistine.android.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import android.widget.Button

class WelcomeFragment : Fragment(R.layout.fragment_welcome) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val button = view.findViewById<Button>(R.id.getStartedButton)
        button.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_email)
        }
    }
}
