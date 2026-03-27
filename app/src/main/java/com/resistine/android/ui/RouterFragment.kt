package com.resistine.android.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.security.CryptoManager
import com.resistine.android.ui.login.LoginViewModel

class RouterFragment : Fragment(R.layout.fragment_router) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()
        val hasStoredRegistration = CryptoManager.isConfigStored(context) ||
            CryptoManager.loadDecryptedEmail(context) != null
        val hasCompletedWelcomeFlow = LoginViewModel.hasCompletedWelcomeFlow(context)

        if (hasStoredRegistration || hasCompletedWelcomeFlow) {
            findNavController().navigate(R.id.action_router_to_home)
        } else {
            findNavController().navigate(R.id.action_router_to_welcome)
        }
    }
}
