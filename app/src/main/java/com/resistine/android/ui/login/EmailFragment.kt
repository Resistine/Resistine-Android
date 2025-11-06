package com.resistine.android.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.resistine.android.R
import android.widget.*
import androidx.navigation.fragment.findNavController


class EmailFragment : Fragment(R.layout.fragment_email) {

    private val viewModel: LoginViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emailInput = view.findViewById<EditText>(R.id.emailInput)
        val sendButton = view.findViewById<Button>(R.id.sendOtpButton)
        val progress = view.findViewById<ProgressBar>(R.id.progressBar)

        viewModel.loading.observe(viewLifecycleOwner) { progress.visibility = if (it) View.VISIBLE else View.GONE }
        viewModel.otpSent.observe(viewLifecycleOwner) {
            if (it == true) {
                findNavController().navigate(R.id.action_email_to_otp)
                viewModel.otpSent.postValue(false) // Reset the value
            }
        }

        sendButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.email.value = email
                viewModel.sendOtp(email)
            } else {
                Toast.makeText(context, getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
