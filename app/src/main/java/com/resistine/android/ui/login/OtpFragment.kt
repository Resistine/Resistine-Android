package com.resistine.android.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.databinding.FragmentOtpBinding
import com.resistine.android.security.CryptoManager

class OtpFragment : Fragment() {
    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()
    private var timer: CountDownTimer? = null
    private lateinit var otpFields: List<EditText>
    private var isResendTimerRunning = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        otpFields = listOf(binding.otp1, binding.otp2, binding.otp3, binding.otp4, binding.otp5, binding.otp6)
        setupOtpFields()

        binding.verifyOtpButton.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            if (otp.length == 6) {
                viewModel.verifyOtp(otp)
            } else {
                Toast.makeText(requireContext(), R.string.please_enter_a_6_digit_code, Toast.LENGTH_SHORT).show()
            }
        }

        binding.changeEmailButton.setOnClickListener {
            viewModel.clearEmail()
            findNavController().navigate(R.id.action_otp_to_email)
        }

        binding.resendButton.setOnClickListener {
            viewModel.email.value?.let(viewModel::sendOtp)
            startResendCountdown(binding.resendButton)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.loadingIndicator.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            val enabled = !state.isLoading
            binding.verifyOtpButton.isEnabled = enabled
            if (!isResendTimerRunning) binding.resendButton.isEnabled = enabled
            binding.changeEmailButton.isEnabled = enabled
            otpFields.forEach { it.isEnabled = enabled }

            if (state.isLoginSuccessful && viewModel.consumeLoginSuccess()) {
                state.email?.let { CryptoManager.saveEmail(requireContext(), it) }
                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.nav_otp) {
                    navController.navigate(R.id.action_otpFragment_to_nav_home)
                }
            }
            if (state.errorMessage != null) {
                viewModel.consumeError()?.let { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }

        startResendCountdown(binding.resendButton)
    }

    private fun setupOtpFields() {
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun afterTextChanged(s: Editable?) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpFields.lastIndex) {
                        otpFields[index + 1].requestFocus()
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() &&
                    index > 0
                ) {
                    otpFields[index - 1].apply {
                        requestFocus()
                        setText("")
                    }
                    true
                } else {
                    false
                }
            }

            if (editText is OtpEditText) {
                editText.setOnPasteListener { pastedText ->
                    otpFields.forEachIndexed { fieldIndex, field ->
                        field.setText(pastedText.getOrNull(fieldIndex)?.toString().orEmpty())
                    }
                    val lastFilled = (pastedText.length - 1).coerceIn(0, otpFields.lastIndex)
                    otpFields[lastFilled].apply {
                        requestFocus()
                        setSelection(text.length)
                    }
                }
            }
        }
    }

    private fun startResendCountdown(button: Button) {
        timer?.cancel()
        button.isEnabled = false
        isResendTimerRunning = true
        timer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(ms: Long) {
                button.text = getString(R.string.resend_with_timer, ms / 1_000)
            }

            override fun onFinish() {
                button.text = getString(R.string.resend)
                isResendTimerRunning = false
                button.isEnabled = viewModel.uiState.value?.isLoading != true
            }
        }.start()
    }

    override fun onDestroyView() {
        timer?.cancel()
        _binding = null
        super.onDestroyView()
    }
}
