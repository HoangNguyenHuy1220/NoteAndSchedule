package com.example.noteandschedule.ui.note

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
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
import com.example.noteandschedule.adapter.note.ImageAdapter
import com.example.noteandschedule.adapter.note.ItemAddNewAdapter
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.data.model.Item
import com.example.noteandschedule.databinding.AddItemDialogBinding
import com.example.noteandschedule.databinding.AddReminderDialogBinding
import com.example.noteandschedule.databinding.DateTimePickerBinding
import com.example.noteandschedule.databinding.FragmentAddNoteBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import java.text.SimpleDateFormat
import java.util.*

const val REQUEST_IMAGE_GET = 1

class AddNoteFragment : Fragment(),
    ImageAdapter.OnSelectedItem,
    ItemAddNewAdapter.OnRemoveItem,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: FragmentAddNoteBinding
    private val navArgs: AddNoteFragmentArgs by navArgs()
    private lateinit var note: Note
    private lateinit var categories: List<Category>
    private lateinit var itemAdapter: ItemAddNewAdapter
    private var check = true

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this, NoteViewModelFactory(activity.application))[NoteViewModel::class.java]
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

                note.categoryId = categories[position].categoryId
                sharedViewModel.updateNote(note)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        callback
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = navArgs.noteId
        if (noteId == -1) {
            sharedViewModel.getNewestNote().observe(this.viewLifecycleOwner) {
                note = it
                onBind()
            }
        }
        else {
            sharedViewModel.getNoteInfo(noteId).observe(this.viewLifecycleOwner) {
                note = it // a note
                onBind()
            }
        }

        val categoryId = navArgs.categoryId
        sharedViewModel.allCategory.observe(this.viewLifecycleOwner) {
            categories = it // list of category
            setUpSpinner(categoryId)
        }
    }

    private fun setUpSpinner(categoryId: Int) {
        val spinnerAdapter = CategorySpinnerAdapter(requireContext(), categories)
        binding.spinCategory.adapter = spinnerAdapter
        binding.spinCategory.setSelection(sharedViewModel.getPositionCategory(categoryId))
    }

    private fun setUpListItem() {
        itemAdapter = ItemAddNewAdapter(this)
        binding.recyclerItem.adapter = itemAdapter
        binding.recyclerItem.layoutManager = LinearLayoutManager(this.context)
        itemAdapter.submitList(note.itemList)
        itemTouchHelper.attachToRecyclerView(binding.recyclerItem)
    }

    private fun setUpListImage() {
        val imageAdapter = ImageAdapter(this)
        binding.recyclerImage.adapter = imageAdapter
        binding.recyclerImage.layoutManager = LinearLayoutManager(
            requireContext(), LinearLayoutManager.HORIZONTAL, false)
        imageAdapter.submitList(note.noteImage)
    }

    @SuppressLint("SimpleDateFormat")
    private fun onBind() {
        binding.apply {
            imagePin.setImageResource(if (note.isPinned) R.drawable.ic_pinned else R.drawable.icon_unpin)
            imageOption.setOnClickListener { showPopUp(it) }
            textViewDate.text = SimpleDateFormat("dd/MM, HH:mm").format(note.dueDate)
            textViewReminder.text = SimpleDateFormat("HH:mm").format(note.noteReminder ?: Date())
            textViewReminder.visibility = if (note.noteReminder != null) View.VISIBLE else View.GONE
            if (check) {
                binding.editTextTitle.setText(note.noteTitle, TextView.BufferType.EDITABLE)
                binding.editTextDescription.setText(note.noteDescription, TextView.BufferType.EDITABLE)
                check = false
            } else {
                binding.editTextTitle.requestFocus()
                showSoftKeyboard(binding.editTextTitle)
            }
            setUpListItem()
            setUpListImage()

            imagePin.setOnClickListener { actionPin() }
            textViewDate.setOnClickListener { setDueDate() }
            textViewAddItem.setOnClickListener { openAddItemDialog() }
            imageSave.setOnClickListener { actionSaveNote() }

            spinCategory.onItemSelectedListener = selectedItemCallback
        }
    }

    private fun actionPin() {
        binding.imagePin.setImageResource(
            if (note.isPinned) R.drawable.icon_unpin else R.drawable.ic_pinned
        )
        note.isPinned = !note.isPinned
        sharedViewModel.updateNote(note)
    }

    // crate new category
    private fun onFirstItemSelected() {
        val dialogBinding = AddItemDialogBinding.inflate(requireActivity().layoutInflater)
        val dialog = initDialog(dialogBinding)
        setUpDialog(dialogBinding)
        dialog.show()

        dialogBinding.textViewSet.isClickable = false
        dialogBinding.editTextItemName.addTextChangedListener {
            dialogBinding.textViewSet.isClickable = dialogBinding.editTextItemName.text.isNotEmpty()
            dialogBinding.textViewSet.setTextColor(
                if (dialogBinding.textViewSet.text.isEmpty() || dialogBinding.textViewSet.text.length > 30)
                    AppCompatResources.getColorStateList(requireContext(), R.color.item_dialog_black)
                else AppCompatResources.getColorStateList(requireContext(), R.color.primary_color)
            )
        }
        dialogBinding.textViewSet.setOnClickListener {
            val newCategory = Category(categoryName = dialogBinding.editTextItemName.text.toString())
            sharedViewModel.insertCategory(newCategory)
            binding.spinCategory.setSelection(categories.size - 1)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    // When an image clicked
    override fun onImageClicked(position: Int) {
        val action = AddNoteFragmentDirections.actionAddNoteFragmentToImageFragment(
            note.noteId,
            position)
        findNavController().navigate(action)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDueDate() {
        val dialogBinding = DateTimePickerBinding.inflate(layoutInflater)
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.show()

        val c = Calendar.getInstance()
        c.time = note.dueDate
        dialogBinding.textViewTime.text = SimpleDateFormat("HH:mm").format(note.dueDate)

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
            note.dueDate = c.time
            sharedViewModel.updateNote(note)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showPopUp(view: View) {
        PopupMenu(context, view).apply {
            setOnMenuItemClickListener(this@AddNoteFragment)
            inflate(R.menu.note_option_menu)
            show()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_reminder -> {
                openReminderDialog()
                true
            }
            R.id.add_image -> {
                checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                true
            }
            R.id.delete -> {
                deleteNote()
                true
            }
            else -> false
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun openReminderDialog() {
        val dialogBinding = AddReminderDialogBinding.inflate(requireActivity().layoutInflater)
        val dialogReminder = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialogReminder.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogReminder.show()

        if (note.noteReminder == null)
            note.noteReminder = note.dueDate
        dialogBinding.textViewEditTime.text =
            SimpleDateFormat("HH:mm").format(note.noteReminder!!)
        dialogBinding.textViewRepeat.text = note.notifyRepeat
        dialogBinding.textViewEditTime.setOnClickListener { openTimePicker(dialogBinding) }
        dialogBinding.textViewRepeat.setOnClickListener { openConfirmationDialog(dialogBinding) }
        dialogBinding.textViewSet.setOnClickListener { setReminder(dialogReminder) }
        dialogBinding.textViewDelete.setOnClickListener { deleteReminder(dialogReminder) }
    }

    @SuppressLint("SimpleDateFormat")
    private fun openTimePicker(dialogBinding: AddReminderDialogBinding) {
        val cal = Calendar.getInstance()
        cal.time = note.noteReminder!!
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(CLOCK_24H)
            .setHour(cal.get(Calendar.HOUR_OF_DAY))
            .setMinute(cal.get(Calendar.MINUTE))
            .setTitleText("Alarm")
            .build()
        timePicker.show(parentFragmentManager, "Set reminder")

        timePicker.addOnPositiveButtonClickListener {
            cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            cal.set(Calendar.MINUTE, timePicker.minute)
            note.noteReminder = cal.time
            dialogBinding.textViewEditTime.text = SimpleDateFormat("HH:mm").format(note.noteReminder!!)
        }

        timePicker.addOnCancelListener {
            dialogBinding.textViewEditTime.text = SimpleDateFormat("HH:mm").format(note.noteReminder!!)
        }
    }

    private fun openConfirmationDialog(dialogBinding: AddReminderDialogBinding) {
        val singleItems = arrayOf("No repeat", "Daily", "Weekly", "Monthly", "Yearly")
        val checkedPosition = 0
        var checkedItem = getString(R.string.no_repeat)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.repeat))
            .setNeutralButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                note.notifyRepeat = checkedItem
                dialogBinding.textViewRepeat.text = note.notifyRepeat
            }
            .setSingleChoiceItems(singleItems, checkedPosition) { _, which ->
                checkedItem = singleItems[which]
            }
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setReminder(dialogReminder : Dialog) {
        sharedViewModel.updateNote(note)
        dialogReminder.dismiss()
    }

    private fun deleteReminder(dialogReminder: Dialog) {
        note.noteReminder = null
        note.notifyRepeat = getString(R.string.no_repeat)
        sharedViewModel.updateNote(note)
        dialogReminder.dismiss()
    }

    private fun checkPermission(permission: String) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) -> {
                addImage()
            }
            else -> ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                REQUEST_IMAGE_GET
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("permission", (requestCode == REQUEST_IMAGE_GET).toString() )
        if (requestCode == REQUEST_IMAGE_GET) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                addImage()
            } else
                Toast.makeText(requireContext(), getString(R.string.denied_reminder), Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun addImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val imageUri = result.data.toString()
                note.noteImage.add(0, imageUri)
            }

            if (data?.clipData != null) {
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri.toString()
                    note.noteImage.add(0, imageUri)
                }
            }

            sharedViewModel.updateNote(note)
        }
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
            dialogBinding.textViewSet.isClickable = dialogBinding.editTextItemName.text.isNotEmpty()
            dialogBinding.textViewSet.setTextColor(
                if (dialogBinding.textViewSet.text.toString().isBlank())
                    AppCompatResources.getColorStateList(requireContext(), R.color.weight_white)
                else AppCompatResources.getColorStateList(requireContext(), R.color.primary_color)
            )
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
            note.itemList.add(newItem)
            sharedViewModel.updateNote(note)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    override fun onDragAction(viewHolder: ItemAddNewAdapter.ViewHolder) {
        itemTouchHelper.startDrag(viewHolder)
    }

    override fun onChecked(position: Int) {
        note.itemList[position].isChecked = !note.itemList[position].isChecked
        sharedViewModel.updateNote(note)
    }

    override fun onRemoveClicked(position: Int) {
        note.itemList.removeAt(position)
        sharedViewModel.updateNote(note)
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm: InputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun actionSaveNote() {
        if (binding.editTextTitle.text.toString().isEmpty()){
            Snackbar.make(binding.imageSave, getString(R.string.title_error), Snackbar.LENGTH_LONG)
                .show()
        }
        else {
            note.noteTitle = binding.editTextTitle.text.toString()
            note.noteDescription = binding.editTextDescription.text.toString()
            sharedViewModel.updateNote(note)
            backToNoteFragment()
        }
    }

    private fun deleteNote() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_note_verify))
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                sharedViewModel.deleteNote(note)
                backToNoteFragment()
            }
            .show()
    }

    private fun backToNoteFragment() {
        findNavController().navigateUp()
    }

}
