package com.sylovestp.firebasetest.testspringrestapp.paging.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.sylovestp.firebasetest.testspringrestapp.dto.UserItem
import com.sylovestp.firebasetest.testspringrestapp.paging.ItemPagingSource
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService

class ItemViewModel(private val apiService: INetworkService) : ViewModel() {

    val items = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { ItemPagingSource(apiService) }
    ).flow.cachedIn(viewModelScope)
}