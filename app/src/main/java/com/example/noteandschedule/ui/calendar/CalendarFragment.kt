package com.example.noteandschedule.ui.calendar

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.calendar.CalendarListNoteAdapter
import com.example.noteandschedule.adapter.calendar.CalendarTaskListAdapter
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.databinding.FragmentCalendarBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var binding: FragmentCalendarBinding
    private lateinit var noteAdapter: CalendarListNoteAdapter
    private lateinit var taskAdapter: CalendarTaskListAdapter
    private var noteList = mutableListOf<Note>()
    private var taskList = mutableListOf<Task>()
    private lateinit var startTime: Calendar // start day time
    private lateinit var endTime: Calendar // end day time
    private var state = true

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this,
            NoteViewModelFactory(activity.application)
        )[NoteViewModel::class.java]
    }

    private val itemTouchHelper by lazy {
        val simpleCallBack = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                taskList[position].taskProgress = when (taskList[position].taskProgress) {
                    getString(R.string.done) -> {
                        if (direction == ItemTouchHelper.LEFT) {
                            getString(R.string.skip)
                        } else {
                            getString(R.string.pending)
                        }
                    }
                    getString(R.string.skip) -> {
                        if (direction == ItemTouchHelper.RIGHT) {
                            getString(R.string.done)
                        } else {
                            getString(R.string.pending)
                        }
                    }
                    else -> {
                        if (direction == ItemTouchHelper.LEFT) {
                            getString(R.string.skip)
                        } else {
                            getString(R.string.done)
                        }
                    }
                }

                taskAdapter.notifyItemChanged(position)
                sharedViewModel.updateTask(taskList[position])
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                var colorMoveLeft = R.color.notification_active
                var colorMoveRight = R.color.complete_color
                var drawableMoveLeft = R.drawable.icon_skip_outline
                var drawableMoveRight = R.drawable.icon_check_circle_outline

                //val position = viewHolder.adapterPosition
                //val progress = taskList[viewHolder.adapterPosition].taskProgress

                Log.d("checktaskList", taskList.size.toString())
                /*when (taskList[position].taskProgress) {
                    getString(R.string.done) -> {
                        colorMoveRight = R.color.light_black
                        drawableMoveRight = R.drawable.icon_reload
                    }
                    getString(R.string.skip) -> {
                        colorMoveLeft = R.color.light_black
                        drawableMoveLeft = R.drawable.icon_reload
                    }
                }*/

                RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                        requireContext(),
                        if (taskList[viewHolder.adapterPosition].taskProgress == getString(R.string.skip)) R.color.light_black
                        else R.color.notification_active
                    ))
                    .addSwipeLeftActionIcon(
                        if (taskList[viewHolder.adapterPosition].taskProgress == getString(R.string.skip)) R.drawable.icon_reload
                        else R.drawable.icon_skip_outline
                    )
                    .addSwipeRightBackgroundColor(
                        ContextCompat.getColor(
                        requireContext(),
                        if (taskList[viewHolder.adapterPosition].taskProgress == getString(R.string.done)) R.color.light_black
                        else R.color.complete_color
                    ))
                    .addSwipeRightActionIcon(
                        if (taskList[viewHolder.adapterPosition].taskProgress == getString(R.string.done)) R.drawable.icon_reload
                        else R.drawable.icon_check_circle_outline
                    )
                    .create()
                    .decorate()

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

            }
        }

        ItemTouchHelper(simpleCallBack)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpNoteRecyclerView()
        setUpTaskRecyclerView()

        itemListToday()
        binding.apply {
            calendar.setOnDateChangeListener { _, year, month, day ->
                startTime.set(year, month, day)
                endTime.set(year, month, day)
                displayNotes()
                displayTasks()
            }
        }
    }

    private fun setUpTaskRecyclerView() {
        taskAdapter = CalendarTaskListAdapter { task ->
            val action = CalendarFragmentDirections.actionCalendarFragmentToCreateTaskFragment(
                task.taskId,
                task.categoryId
            )
            findNavController().navigate(action)
        }
        binding.recyclerTask.adapter = taskAdapter
        binding.recyclerTask.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTask.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        itemTouchHelper.attachToRecyclerView(binding.recyclerTask)
    }

    private fun setUpNoteRecyclerView() {
        binding.recyclerNote.visibility = View.GONE
        noteAdapter = CalendarListNoteAdapter(noteList) { note ->
            val action = CalendarFragmentDirections.actionCalendarFragmentToAddNoteFragment(
                note.noteId,
                note.categoryId
            )
            findNavController().navigate(action)
        }
        binding.recyclerNote.adapter = noteAdapter
        binding.recyclerNote.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun itemListToday() {
        startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        endTime = Calendar.getInstance()
        endTime.set(Calendar.HOUR_OF_DAY, 23)
        endTime.set(Calendar.MINUTE, 59)
        endTime.set(Calendar.SECOND, 59)

        displayNotes()
        displayTasks()
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun displayNotes() {
        val cal = Calendar.getInstance()
        binding.textDate.text =
            if (startTime.get(Calendar.DATE) == cal.get(Calendar.DATE)
                && startTime.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                && startTime.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                getString(R.string.today)
            } else {
                SimpleDateFormat("MMMM dd").format(startTime.time)
            }
        sharedViewModel.getNotesByDate(startTime.time, endTime.time).observe(viewLifecycleOwner, { notes ->
            noteList = notes.toMutableList()
            arrangePin()
            noteAdapter.setNoteList(noteList)
            noteAdapter.notifyDataSetChanged()
        })
    }

    @SuppressLint("SimpleDateFormat", "NotifyDataSetChanged")
    private fun displayTasks() {
        val cal = Calendar.getInstance()
        binding.textDate.text =
            if (startTime.get(Calendar.DATE) == cal.get(Calendar.DATE)
                && startTime.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                && startTime.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
                getString(R.string.today)
            } else {
                SimpleDateFormat("MMMM dd").format(startTime.time)
            }
        sharedViewModel.getAllTask(startTime.time, endTime.time).observe(viewLifecycleOwner, { tasks ->
            taskList = tasks.toMutableList()
            taskAdapter.submitList(taskList)
        })
    }

    // arrange pinned notes
    private fun arrangePin() {
        val newList = mutableListOf<Note>()
        var i = 0
        while (i < noteList.size) {
            if (noteList[i].isPinned) {
                newList.add(noteList[i])
                noteList.removeAt(i)
            } else
                i += 1
        }
        noteList.addAll(0, newList)
    }

}