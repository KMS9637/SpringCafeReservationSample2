package com.sylovestp.firebasetest.testspringrestapp.paging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sylovestp.firebasetest.testspringrestapp.R
import com.sylovestp.firebasetest.testspringrestapp.dto.UserItem


class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val usernameTextView: TextView = itemView.findViewById(R.id.user_username)
    private val emailTextView: TextView = itemView.findViewById(R.id.user_email)
    private val profileImageView: ImageView = itemView.findViewById(R.id.user_profile)

    fun bind(item: UserItem?) {
        if (item != null) {
            usernameTextView.text = item.username
            emailTextView.text = item.email

            // 프로필 이미지 로드 (예: Glide 사용)
            Glide.with(itemView.context)
                .load("http://localhost:8080/users/"+item.profileImageId+"/profileImage")  // profileImageId는 URL 또는 리소스 ID일 수 있습니다
                .placeholder(R.drawable.user_profile_)  // 기본 이미지
                .into(profileImageView)
        } else {
            // item이 null일 경우 기본 값을 설정하거나 처리할 수 있습니다.
            usernameTextView.text = "Unknown"
            emailTextView.text = "Unknown Email"
            profileImageView.setImageResource(R.drawable.user_profile_)
        }
    }
}


class ItemAdapter : PagingDataAdapter<UserItem, ItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserItem>() {
            override fun areItemsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UserItem, newItem: UserItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}