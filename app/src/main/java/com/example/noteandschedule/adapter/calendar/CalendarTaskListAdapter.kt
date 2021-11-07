package com.example.noteandschedule.adapter.calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.R
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.databinding.CalendarTaskListItemBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarTaskListAdapter(private val onSelected: (Task) -> Unit)
    : ListAdapter<Task, CalendarTaskListAdapter.TaskViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
                return oldItem.taskId == newItem.taskId
            }
        }
    }

    class TaskViewHolder(private val binding: CalendarTaskListItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun onBind(task: Task, context: Context) {
            binding.apply {
                textTime.text = SimpleDateFormat("HH:mm").format(task.startTime)
                imageReminder.visibility =
                    if (task.taskReminder != null) View.VISIBLE else View.GONE
                textName.text = task.taskName
                if (task.taskDescription.isNotEmpty()) {
                    textDescription.visibility = View.VISIBLE
                    textDescription.text = task.taskDescription
                }
                if (task.itemList.isNotEmpty()) {
                    setUpCheckList(task, context)
                    textCheckList.visibility = View.VISIBLE
                }
                //textCategory.text = task
                textProgress.text = task.taskProgress
                setTextProgressColor(task, context)
            }
        }

        private fun setUpCheckList(task: Task, context: Context) {
            var checkedItem = 0
            task.itemList.forEach { item ->
                if (item.isChecked)
                    checkedItem += 1
            }
            binding.textCheckList.text =
                context.getString(R.string.check_list, checkedItem, task.itemList.size)

            if (checkedItem == task.itemList.size) {
                binding.textCheckList.background =
                    ContextCompat.getDrawable(context, R.drawable.check_list_bg)
                binding.textCheckList.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_outline_check_box_white, 0, 0 ,0
                )
                binding.textCheckList.setTextColor(
                    ContextCompat.getColor(context, android.R.color.white)
                )
            }
        }

        private fun setTextProgressColor(task: Task, context: Context) {
            when (task.taskProgress) {
                context.getString(R.string.done) -> {
                    binding.textProgress.setTextColor(
                        ContextCompat.getColor(context, R.color.complete_color)
                    )
                }
                context.getString(R.string.skip) -> {
                    binding.textProgress.setTextColor(
                        ContextCompat.getColor(context, R.color.notification_active)
                    )
                }
                else -> {
                    binding.textProgress.setTextColor(
                        ContextCompat.getColor(context, R.color.black)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = CalendarTaskListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.onBind(currentTask, holder.itemView.context)
        holder.itemView.setOnClickListener {
            onSelected(currentTask)
        }
    }
}