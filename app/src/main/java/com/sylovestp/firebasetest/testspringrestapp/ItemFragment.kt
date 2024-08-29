package com.sylovestp.firebasetest.testspringrestapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sylovestp.firebasetest.testspringrestapp.paging.adapter.ItemAdapter
import com.sylovestp.firebasetest.testspringrestapp.paging.viewModel.ItemViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.fragment.app.viewModels // Fragment에서 사용


class ItemFragment : Fragment() {

    private val viewModel: ItemViewModel by viewModels()
    private lateinit var itemAdapter: ItemAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemAdapter = ItemAdapter()
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = itemAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        lifecycleScope.launchWhenStarted {
            viewModel.items.collectLatest { pagingData ->
                itemAdapter.submitData(pagingData)
            }
        }

    }

}