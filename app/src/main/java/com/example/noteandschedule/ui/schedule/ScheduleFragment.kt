package com.example.noteandschedule.ui.schedule

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.schedule.TaskAdapter
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.databinding.FragmentScheduleBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*


class ScheduleFragment : Fragment() {

    private lateinit var binding: FragmentScheduleBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var allTask: MutableList<Task>
    private lateinit var startTime: Calendar // start day time
    private lateinit var endTime: Calendar // end day time

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this,
            NoteViewModelFactory(activity.application)
        )[NoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setUpTaskList() {
        adapter = TaskAdapter ({ task ->
            val action = ScheduleFragmentDirections.actionScheduleFragmentToCreateTaskFragment(
                task.taskId,
                task.categoryId)
            findNavController().navigate(action)
        } , { view, position ->
            showPopUp(view, position)
        }
            )
        binding.recyclerTaskList.adapter = adapter
        binding.recyclerTaskList.layoutManager = LinearLayoutManager(requireContext())
        adapter.submitList(allTask)
    }

    private fun showPopUp(v: View, position: Int) {
        PopupMenu(v.context, v).apply {
            inflate(R.menu.task_progress_menu)
            setOnMenuItemClickListener { item ->
                onChangeState(item, position)
            }
            show()
        }
    }

    private fun onChangeState(item: MenuItem, position: Int): Boolean {
        allTask[position].taskProgress = when (item.itemId) {
            R.id.pending -> {
                getString(R.string.pending)
            }
            R.id.done -> {
                getString(R.string.done)
            }
            else -> {
                getString(R.string.skip)
            }
        }
        sharedViewModel.updateTask(allTask[position])
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskListToday()

        binding.buttonPrevious.setOnClickListener { actionChangeTime(true) }
        binding.buttonNext.setOnClickListener { actionChangeTime(false) }
        binding.buttonFilter.setOnClickListener { openDialog() }
        binding.buttonToday.setOnClickListener { taskListToday() }
        binding.buttonAddTask.setOnClickListener {
            val newTask = Task()
            sharedViewModel.insertTask(newTask)
            val action =
                ScheduleFragmentDirections.actionScheduleFragmentToCreateTaskFragment(category = newTask.categoryId)
            findNavController().navigate(action)
        }

    }

    // init and display tasks today
    private fun taskListToday() {
        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, 23)
        endTime.set(Calendar.MINUTE, 59)
        endTime.set(Calendar.SECOND, 59)

        displayTasks()
    }

    @SuppressLint("SimpleDateFormat")
    private fun displayTasks() {
        val cal = Calendar.getInstance()
        binding.textMonth.text =
            if (startTime.get(Calendar.DATE) == cal.get(Calendar.DATE)
                && startTime.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                && startTime.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                    getString(R.string.today)
            } else {
                SimpleDateFormat("EEE, MMM dd").format(startTime.time)
            }
        sharedViewModel.getAllTask(startTime.time, endTime.time).observe(viewLifecycleOwner, { tasks ->
            allTask = tasks.toMutableList()
            setUpProgress()
            setUpTaskList()
        })
    }

    private fun setUpProgress() {
        var complete = 0
        var skip = 0
        allTask.forEach {
            if (it.taskProgress == getString(R.string.done)) {
                complete += 1
            }
            if (it.taskProgress == getString(R.string.skip)) {
                skip += 1
            }
        }
        binding.textAllTask.text = allTask.size.toString()
        binding.textDoneTask.text = complete.toString()
        binding.textSkipTask.text = skip.toString()
    }

    private fun openDialog() {
        val options = resources.getStringArray(R.array.array_option_filter)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.filter_by))
            .setItems(options) { _, which ->
                filterTasks(options[which])
            }
            .show()
    }

    private fun filterTasks(progress: String) {
        if (progress == getString(R.string.all)) {
            displayTasks()
        } else {
            sharedViewModel.getTasksByProgress(progress, startTime.time, endTime.time)
                .observe(viewLifecycleOwner, {
                    allTask = it.toMutableList()
                    adapter.submitList(allTask)
                })
        }
    }

    private fun actionChangeTime(direction : Boolean) {
        if (direction) { // previous date clicked
            startTime.add(Calendar.DATE, -1)
            endTime.add(Calendar.DATE, -1)
        } else { // next date clicked
            startTime.add(Calendar.DATE, 1)
            endTime.add(Calendar.DATE, 1)
        }
        displayTasks()
    }
}