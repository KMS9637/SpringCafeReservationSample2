package com.sylovestp.firebasetest.testspringrestapp.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.sylovestp.firebasetest.testspringrestapp.dto.UserItem
import com.sylovestp.firebasetest.testspringrestapp.retrofit.INetworkService

class ItemPagingSource(
    private val apiService: INetworkService
) : PagingSource<Int, UserItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserItem> {
        return try {
            val position = params.key ?: 0
            val response = apiService.getItems(position, params.loadSize)

            val items = response.body()?.content ?: emptyList()
            LoadResult.Page(
                data = items,
                prevKey = if (position == 0) null else position - 1,
                nextKey = if (items.isEmpty()) null else position + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, UserItem>): Int? {
        TODO("Not yet implemented")
    }
}