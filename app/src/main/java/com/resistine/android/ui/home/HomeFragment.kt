package com.resistine.android.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.resistine.android.R

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeCardAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        recyclerView = view.findViewById(R.id.recyclerView_dashboard)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.cards.observe(viewLifecycleOwner) { items ->
            adapter = HomeCardAdapter(items) { item ->
                findNavController().navigate(item.destinationFragmentId)
            }
            recyclerView.adapter = adapter
        }
    }
}
