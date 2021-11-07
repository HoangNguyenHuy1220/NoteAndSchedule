package com.example.noteandschedule.ui.schedule

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.CategorySpinnerAdapter
import com.example.noteandschedule.adapter.note.ItemAddNewAdapter
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Task
import com.example.noteandschedule.data.model.Item
import com.example.noteandschedule.databinding.AddItemDialogBinding
import com.example.noteandschedule.databinding.DateTimePickerBinding
import com.example.noteandschedule.databinding.FragmentCreateTaskBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*


class CreateTaskFragment : Fragment(), ItemAddNewAdapter.OnRemoveItem {

    private lateinit var binding : FragmentCreateTaskBinding
    private val navArgs : CreateTaskFragmentArgs by navArgs()
    private lateinit var spinnerAdapter: CategorySpinnerAdapter
    private var listCategory = mutableListOf<Category>()
    private lateinit var task: Task
    private var check = true
    private lateinit var itemAdapter: ItemAddNewAdapter

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this,
            NoteViewModelFactory(activity.application)
        )[NoteViewModel::class.java]
    }

    private val selectedItemCallback by lazy {
        val callback = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    onFirstItemSelected()
                }
                else
                    binding.spinCategory.setSelection(position)

                task.categoryId = listCategory[position].categoryId
                sharedViewModel.updateTask(task)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        callback
    }

    private val itemTouchHelper by lazy {
        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                itemAdapter.notifyItemMoved(fromPos, toPos)
                Collections.swap(task.itemList, fromPos, toPos)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        }

        ItemTouchHelper(simpleCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = navArgs.category
        sharedViewModel.allCategory.observe(viewLifecycleOwner, { categories ->
            listCategory = categories.toMutableList()
            setUpSpinner(categoryId)
        })

        val taskId = navArgs.taskId
        if (taskId == -1) {
            sharedViewModel.getNewestTask().observe(viewLifecycleOwner, {
                task = it
                onBind(task)
            })
        } else {
            sharedViewModel.getTaskInfo(taskId).observe(viewLifecycleOwner, {
                task = it
                onBind(task)
            })
        }
    }

    private fun setUpSpinner(categoryId: Int) {
        spinnerAdapter = CategorySpinnerAdapter(requireContext(), listCategory)
        binding.spinCategory.adapter = spinnerAdapter
        binding.spinCategory.setSelection(
            sharedViewModel.getPositionCategory(categoryId)
        )
    }

    private fun setUpListItem() {
        itemAdapter = ItemAddNewAdapter(this)
        binding.recyclerSubTask.adapter = itemAdapter
        binding.recyclerSubTask.layoutManager = LinearLayoutManager(requireContext())
        itemAdapter.submitList(task.itemList)
        itemTouchHelper.attachToRecyclerView(binding.recyclerSubTask)
    }

    @SuppressLint("SimpleDateFormat")
    private fun onBind(task: Task) {
        binding.apply {
            if (check) {
                editTextName.setText(task.taskName, TextView.BufferType.EDITABLE)
                editTextDescription.setText(task.taskDescription, TextView.BufferType.EDITABLE)
                check = false
            }
            switchSubTask.isChecked = task.itemList.isNotEmpty()
            layoutSubTask.visibility = if (task.itemList.isNotEmpty()) View.VISIBLE else View.GONE
            setUpListItem()
            switchAllDay.isChecked = task.allDay
            textStartDate.text = SimpleDateFormat("EEE, MMM dd, yyyy").format(task.startTime.time)
            textEndDate.text = SimpleDateFormat("EEE, MMM dd, yyyy").format(task.endTime.time)
            textStartTime.visibility = if (task.allDay) View.GONE else View.VISIBLE
            textEndTime.visibility = if (task.allDay) View.GONE else View.VISIBLE
            textStartTime.text = SimpleDateFormat("HH:mm").format(task.startTime.time)
            textEndTime.text = SimpleDateFormat("HH:mm").format(task.endTime.time)

            textRepeat.text = task.taskRepeat
            textReminder.text =
                if (task.taskReminder != null) SimpleDateFormat("HH:mm").format(task.taskReminder!!)
                else getString(R.string.add_notification)

            spinCategory.onItemSelectedListener = selectedItemCallback
            imageSave.setOnClickListener { actionSaveTask() }
            imageDelete.setOnClickListener { actionDeleteTask() }
            switchSubTask.setOnCheckedChangeListener { _, _ ->
                layoutSubTask.visibility =
                    if (layoutSubTask.isVisible) View.GONE
                    else View.VISIBLE
            }
            textAddItem.setOnClickListener { openAddItemDialog() }
            switchAllDay.setOnCheckedChangeListener { _, isChecked ->
                task.allDay = isChecked
                sharedViewModel.updateTask(task)
            }
            layoutStartTime.setOnClickListener { setUpTimeDialog(task.startTime, 1) }
            layoutEndTime.setOnClickListener { setUpTimeDialog(task.endTime, 2) }
            imageEditRepeat.setOnClickListener { actionSetRepeat() }
            imageEditReminder.setOnClickListener { actionSetReminder() }
        }
    }

    private fun openAddItemDialog() {
        val dialogBinding = AddItemDialogBinding.inflate(requireActivity().layoutInflater)
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogBinding.editTextItemName.requestFocus()
        showSoftKeyboard(dialogBinding.editTextItemName)
        dialogBinding.textViewTitle.text = getString(R.string.new_item)
        dialogBinding.textViewSet.setTextColor(ContextCompat.getColor(requireContext(), R.color.weight_white))
        dialogBinding.textViewSet.isClickable = false
        dialogBinding.textViewCharacter.text = getString(R.string.character_quantity, 0)

        dialogBinding.editTextItemName.addTextChangedListener {
            dialogBinding.textViewCharacter.text = getString(
                R.string.character_quantity, dialogBinding.editTextItemName.text.length)
            dialogBinding.textViewSet.isClickable = dialogBinding.editTextItemName.text.isNotEmpty()
            dialogBinding.textViewSet.setTextColor(
                if (dialogBinding.textViewSet.text.toString().isBlank())
                    ContextCompat.getColor(requireContext(), R.color.weight_white)
                else
                    ContextCompat.getColor(requireContext(), R.color.primary_color)
            )
        }

        dialogBinding.textViewSet.setOnClickListener {
            val newItem = Item(name = dialogBinding.editTextItemName.text.toString())
            task.itemList.add(newItem)
            sharedViewModel.updateTask(task)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setUpTimeDialog(time: Date, setWith: Int) {
        val dialogBinding = DateTimePickerBinding.inflate(layoutInflater)
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.show()

        val c = Calendar.getInstance()
        c.time = time
        dialogBinding.textViewTime.text = SimpleDateFormat("HH:mm").format(c.time)
        dialogBinding.datePicker.init(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ) { _, year, monthOfYear, dayOfMonth ->
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        dialogBinding.timePicker.setIs24HourView(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialogBinding.timePicker.hour = c.get(Calendar.HOUR_OF_DAY)
            dialogBinding.timePicker.minute = c.get(Calendar.MINUTE)
        }
        dialogBinding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            c.set(Calendar.HOUR_OF_DAY, hourOfDay)
            c.set(Calendar.MINUTE, minute)
        }

        dialogBinding.textViewTime.setOnClickListener {
            if (dialogBinding.timePicker.isVisible) {
                dialogBinding.timePicker.visibility = View.GONE
                dialogBinding.datePicker.visibility = View.VISIBLE
                dialogBinding.textViewTime.text = SimpleDateFormat("HH:mm").format(c.time)
            }
            else {
                dialogBinding.timePicker.visibility = View.VISIBLE
                dialogBinding.datePicker.visibility = View.GONE
                dialogBinding.textViewTime.text = SimpleDateFormat("MMM dd, yyyy").format(c.time)
            }
        }
        dialogBinding.textViewSet.setOnClickListener {
            if (setWith == 1)
                task.startTime = c.time
            else
                task.endTime = c.time
            sharedViewModel.updateTask(task)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun actionSetRepeat() {
        val items = resources.getStringArray(R.array.array_repeat)
        var position = 0
        for (i in 0 until items.size-1) {
            if (items[i] == task.taskRepeat) {
                position = i
                break
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setSingleChoiceItems(items, position) { _, which ->
                task.taskRepeat = items[which]
                sharedViewModel.updateTask(task)
            }
            .show()
    }

    private fun actionSetReminder() {
        val cal = Calendar.getInstance()
        cal.time = task.taskReminder ?: task.startTime
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(cal.get(Calendar.HOUR_OF_DAY))
            .setMinute(cal.get(Calendar.MINUTE))
            .build()
        picker.show(parentFragmentManager, "Reminder")

        picker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, picker.hour)
            cal.set(Calendar.MINUTE, picker.minute)
            task.taskReminder = cal.time
            sharedViewModel.updateTask(task)
        }
    }

    private fun onFirstItemSelected() {
        val dialogBinding = AddItemDialogBinding.inflate(requireActivity().layoutInflater)
        val dialog = initDialog(dialogBinding)
        setUpDialog(dialogBinding)
        dialog.show()

        dialogBinding.textViewSet.isClickable = false
        dialogBinding.textViewTitle.text = getString(R.string.add_new_category)

        dialogBinding.editTextItemName.addTextChangedListener {
            dialogBinding.textViewSet.isClickable = dialogBinding.editTextItemName.text.isEmpty()
            dialogBinding.textViewSet.setTextColor(
                if (dialogBinding.textViewSet.text.isEmpty() || dialogBinding.textViewSet.text.length > 30)
                    AppCompatResources.getColorStateList(requireContext(), R.color.item_dialog_black)
                else AppCompatResources.getColorStateList(requireContext(), R.color.primary_color)
            )
        }

        dialogBinding.textViewSet.setOnClickListener {
            val newCategory = Category(categoryName = dialogBinding.editTextItemName.text.toString())
            sharedViewModel.insertCategory(newCategory)
            binding.spinCategory.setSelection(listCategory.size - 1)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun initDialog(dialogBinding: AddItemDialogBinding): Dialog {
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        return dialog
    }

    private fun setUpDialog(dialogBinding: AddItemDialogBinding) {
        dialogBinding.editTextItemName.requestFocus()
        showSoftKeyboard(dialogBinding.editTextItemName)
        dialogBinding.textViewSet.setTextColor(
            AppCompatResources.getColorStateList(requireContext(), R.color.weight_white)
        )
        dialogBinding.textViewSet.isClickable = false
        dialogBinding.textViewCharacter.text = getString(R.string.character_quantity, 0)

        dialogBinding.editTextItemName.addTextChangedListener {
            dialogBinding.textViewCharacter.text = getString(
                R.string.character_quantity, dialogBinding.editTextItemName.text.length)
            dialogBinding.textViewSet.isClickable = dialogBinding.editTextItemName.text.isEmpty()
            dialogBinding.textViewSet.setTextColor(
                if (dialogBinding.textViewSet.text.toString().isBlank())
                    AppCompatResources.getColorStateList(requireContext(), R.color.weight_white)
                else AppCompatResources.getColorStateList(requireContext(), R.color.primary_color)
            )
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm: InputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun actionDeleteTask() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_note_verify))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                sharedViewModel.deleteTask(task)
                backToScheduleFragment()
            }
            .show()
    }

    override fun onDragAction(viewHolder: ItemAddNewAdapter.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onChecked(position: Int) {
        task.itemList[position].isChecked = !task.itemList[position].isChecked
        sharedViewModel.updateTask(task)
    }

    override fun onRemoveClicked(position: Int) {
        task.itemList.removeAt(position)
        sharedViewModel.updateTask(task)
    }

    private fun actionSaveTask() {
        if (binding.editTextName.text.isEmpty()) {
            Snackbar.make(binding.imageSave, getString(R.string.task_name_required), Snackbar.LENGTH_LONG)
                .show()
        } else {
            task.taskName = binding.editTextName.text.toString()
            task.taskDescription = binding.editTextDescription.text.toString()
            if (!binding.switchSubTask.isChecked && task.itemList.isNotEmpty()) {
                task.itemList.clear()
            }
            sharedViewModel.updateTask(task)
            backToScheduleFragment()
        }
    }

    private fun backToScheduleFragment() {
        findNavController().navigateUp()
    }
}