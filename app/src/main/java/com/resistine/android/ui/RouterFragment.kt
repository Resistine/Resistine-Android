package com.resistine.android.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.security.CryptoManager

class RouterFragment : Fragment(R.layout.fragment_router) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isLoggedIn = CryptoManager.isConfigStored(requireContext())

        if (isLoggedIn) {
            findNavController().navigate(R.id.action_router_to_home)
        } else {
            findNavController().navigate(R.id.action_router_to_welcome)
        }
    }
}
