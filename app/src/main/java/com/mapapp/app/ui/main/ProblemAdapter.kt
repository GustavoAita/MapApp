package com.mapapp.app.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mapapp.app.data.model.Problem
import com.mapapp.app.databinding.ItemProblemBinding

class ProblemAdapter(
    private val onItemClick: (Problem) -> Unit
) : ListAdapter<Problem, ProblemAdapter.ProblemViewHolder>(ProblemDiffCallback()) {

    inner class ProblemViewHolder(
        private val binding: ItemProblemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(problem: Problem) {
            binding.apply {
                tvCategoryEmoji.text = problem.getCategoryEmoji()
                tvTitle.text = problem.title
                tvCategory.text = problem.category
                tvDescription.text = problem.description
                tvUserName.text = problem.userName
                tvTime.text = problem.getTimeAgo()
                tvVotes.text = "👍 ${problem.votesCount}"

                root.setOnClickListener {
                    onItemClick(problem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProblemViewHolder {
        val binding = ItemProblemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProblemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProblemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProblemDiffCallback : DiffUtil.ItemCallback<Problem>() {
        override fun areItemsTheSame(oldItem: Problem, newItem: Problem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Problem, newItem: Problem): Boolean {
            return oldItem == newItem
        }
    }
}