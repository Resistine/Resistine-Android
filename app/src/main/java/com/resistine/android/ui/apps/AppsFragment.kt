package com.resistine.android.ui.apps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.resistine.android.databinding.FragmentAppsBinding

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AppsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[AppsViewModel::class.java]

        viewModel.apps.observe(viewLifecycleOwner) { apps ->
            val adapter = AppAdapter(apps, requireContext().packageManager)
            binding.recyclerViewApps.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewApps.adapter = adapter
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
