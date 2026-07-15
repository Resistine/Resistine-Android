package com.resistine.android.ui.login

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.resistine.android.R
import com.resistine.android.databinding.FragmentEmailBinding

class EmailFragment : Fragment() {
    private var _binding: FragmentEmailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.sendOtpButton.setOnClickListener {
            val email = binding.emailInput.text?.toString()?.trim().orEmpty()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                viewModel.email.value = email
                viewModel.sendOtp(email)
            } else {
                binding.emailInputLayout.error = getString(R.string.please_enter_valid_email)
            }
        }

        binding.skipButton.setOnClickListener {
            viewModel.skipRegistration()
            findNavController().navigate(R.id.action_email_to_home)
        }

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.sendOtpButton.isEnabled = !state.isLoading
            binding.skipButton.isEnabled = !state.isLoading
            binding.emailInput.isEnabled = !state.isLoading

            if (state.isOtpSent && viewModel.consumeOtpSent()) {
                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.nav_email) {
                    navController.navigate(R.id.action_email_to_otp)
                }
            }
            if (state.errorMessage != null) {
                viewModel.consumeError()?.let { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
