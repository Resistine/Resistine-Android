package com.resistine.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.resistine.android.R
import com.resistine.android.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val adapter = HomeCardAdapter { item ->
        findNavController().navigate(item.destinationFragmentId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerViewDashboard.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDashboard.adapter = adapter
        viewModel.uiState.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: HomeUiState) {
        binding.textProtectionState.text = getString(
            if (state.isProtected) R.string.home_protected else R.string.home_action_needed
        )
        binding.textProtectionSummary.text = getString(
            if (state.isProtected) R.string.home_protection_summary else R.string.home_protection_attention
        )
        binding.textSecurityScore.text = state.securityScore.toString()
        binding.textThreatCount.text = state.threatCount.toString()
        binding.textLastScan.text = state.lastScanLabel
        binding.imageViewHome.clearColorFilter()
        adapter.submitList(state.cards)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        binding.recyclerViewDashboard.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
