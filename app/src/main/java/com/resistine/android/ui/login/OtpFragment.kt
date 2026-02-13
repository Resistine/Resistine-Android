package com.resistine.android.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
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
    private lateinit var otpFields: List<EditText>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        otpFields = listOf(
            view.findViewById(R.id.otp1),
            view.findViewById(R.id.otp2),
            view.findViewById(R.id.otp3),
            view.findViewById(R.id.otp4),
            view.findViewById(R.id.otp5),
            view.findViewById(R.id.otp6)
        )

        val verifyButton = view.findViewById<Button>(R.id.verifyOtpButton)
        val resendButton = view.findViewById<Button>(R.id.resendButton)
        val changeEmailButton = view.findViewById<Button>(R.id.changeEmailButton)
        val loadingIndicator = view.findViewById<ProgressBar>(R.id.loadingIndicator)

        setupOtpFields()

        verifyButton.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            if (otp.length == 6) {
                viewModel.verifyOtp(otp)
            } else {
                Toast.makeText(context, getString(R.string.please_enter_a_6_digit_code), Toast.LENGTH_SHORT).show()
            }
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
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            verifyButton.isEnabled = !isLoading
            resendButton.isEnabled = !isLoading
            changeEmailButton.isEnabled = !isLoading
            otpFields.forEach { it.isEnabled = !isLoading }
        }

        startResendCountdown(resendButton)
    }

    private fun setupOtpFields() {
        otpFields.forEachIndexed { index, editText ->
            // Handle typing
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle deleting
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus()
                        otpFields[index - 1].setText("")
                        return@setOnKeyListener true
                    }
                }
                false
            }

            // Handle pasting
            if (editText is OtpEditText) {
                editText.setOnPasteListener { pastedText ->
                    otpFields.forEachIndexed { i, field ->
                        val textToSet = if (i < pastedText.length) pastedText[i].toString() else ""
                        field.setText(textToSet)
                    }
                    val lastFilledIndex = (pastedText.length - 1).coerceIn(0, otpFields.size - 1)
                    otpFields[lastFilledIndex].requestFocus()
                    otpFields[lastFilledIndex].setSelection(otpFields[lastFilledIndex].text.length)
                }
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}
