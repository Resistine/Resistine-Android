package com.resistine.android.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.security.CryptoManager

class OtpFragment : Fragment(R.layout.fragment_otp) {

    private val viewModel: LoginViewModel by activityViewModels()
    private var timer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val otpInput = view.findViewById<EditText>(R.id.otpInput)
        val verifyButton = view.findViewById<Button>(R.id.verifyOtpButton)
        val resendButton = view.findViewById<Button>(R.id.resendButton)
        val changeEmailButton = view.findViewById<Button>(R.id.changeEmailButton)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)

        verifyButton.setOnClickListener {
            val otp = otpInput.text.toString()
            viewModel.verifyOtp(otp)
        }

        changeEmailButton.setOnClickListener {
            viewModel.email = MutableLiveData<String>(null)
            findNavController().navigate(R.id.action_otp_to_email)
        }

        resendButton.setOnClickListener {
            viewModel.email.value?.let { viewModel.sendOtp(it) }
            startResendCountdown(resendButton)
        }

        viewModel.loginSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                viewModel.email.value?.let { email ->
                    CryptoManager.saveEmail(requireContext(), email)
                }
                findNavController().navigate(R.id.action_otpFragment_to_nav_home)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                viewModel.errorMessage.postValue(null)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                loadingIndicator.visibility = View.VISIBLE
                verifyButton.isEnabled = false
            } else {
                loadingIndicator.visibility = View.GONE
                verifyButton.isEnabled = true
            }
        }

        startResendCountdown(resendButton)
    }

    private fun startResendCountdown(button: Button) {
        timer?.cancel()
        button.isEnabled = false
        timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(ms: Long) {
                button.text = getString(R.string.resend_with_timer, ms / 1000)
            }

            override fun onFinish() {
                button.isEnabled = true
                button.text = getString(R.string.resend)
            }
        }.start()
    }
}
