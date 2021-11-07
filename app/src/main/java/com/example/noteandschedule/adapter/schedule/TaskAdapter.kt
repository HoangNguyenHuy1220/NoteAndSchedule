package com.example.noteandschedule.adapter.schedule

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
import com.example.noteandschedule.databinding.TaskItemLayoutBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onSelected: (Task) -> Unit,
    private val showPopupMenu: (View, Int) -> Unit)
    : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback) {

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

    class TaskViewHolder(val binding: TaskItemLayoutBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun onBind(task: Task, size: Int, context: Context) {
            binding.apply {
                if (adapterPosition == size-1) {
                    view.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                }
                textTime.text = SimpleDateFormat("HH:mm").format(task.startTime)
                imageReminder.visibility =
                    if (task.taskReminder != null) View.VISIBLE else View.GONE
                setDotTimeLine(task, context)
                setDotProgress(task, context)
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
            }
        }

        private fun setDotProgress(task: Task, context: Context) {
            binding.buttonSetProgress.setImageResource(
                when (task.taskProgress) {
                    context.getString(R.string.pending) -> R.drawable.icon_circle_pending
                    context.getString(R.string.done) -> R.drawable.icon_circle_done
                    else -> R.drawable.icon_circle_skip
                }
            )
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

        private fun setDotTimeLine(task: Task, context: Context) {
            val cal = Calendar.getInstance()
            if (cal.time.after(task.startTime) && cal.time.before(task.endTime)
                && task.taskProgress == context.getString(R.string.pending)) {
                binding.progressDot.setImageResource(R.drawable.icon_circle_timeline)
                binding.textTime.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.textName.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.textDescription.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.textCheckList.setTextColor(ContextCompat.getColor(context, R.color.black))
                binding.textCategory.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.onBind(currentTask, currentList.size, holder.itemView.context)
        holder.itemView.setOnClickListener {
            onSelected(currentTask)
        }
        holder.binding.buttonSetProgress.setOnClickListener {
            showPopupMenu(holder.binding.buttonSetProgress, position)
        }
    }


}