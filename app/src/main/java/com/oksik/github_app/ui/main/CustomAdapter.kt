package com.oksik.github_app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.oksik.github_app.databinding.MainListCommitItemBinding
import com.oksik.github_app.model.CommitItem

class CustomAdapter(private val clickListener: CommitItemListener) :
    ListAdapter<CommitItem, CustomAdapter.ViewHolder>(
        CommitItemDiffCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(
            parent
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class ViewHolder private constructor(private val binding: MainListCommitItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommitItem, clickListener: CommitItemListener) {
            binding.commitItem = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = MainListCommitItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class CommitItemDiffCallback : DiffUtil.ItemCallback<CommitItem>() {
    override fun areItemsTheSame(oldItem: CommitItem, newItem: CommitItem): Boolean {
        return oldItem.sha == newItem.sha
    }

    override fun areContentsTheSame(oldItem: CommitItem, newItem: CommitItem): Boolean {
        return oldItem == newItem
    }
}

class CommitItemListener(val clickListener: (item: CommitItem) -> Unit) {
    fun onClick(commitItem: CommitItem) = clickListener(commitItem)
}