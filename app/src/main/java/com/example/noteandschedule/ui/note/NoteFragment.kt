package com.example.noteandschedule.ui.note

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.note.NoteAdapter
import com.example.noteandschedule.adapter.note.NoteCategoryAdapter
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.databinding.FragmentNoteBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*


class NoteFragment : Fragment(),
    androidx.appcompat.widget.SearchView.OnQueryTextListener {

    private lateinit var binding: FragmentNoteBinding
    private lateinit var adapter: NoteAdapter
    private lateinit var listNote: MutableList<Note>
    private var categoryId = 2 // 2 means category is all
    var isMultiSelectMode = false
    private val selectedNoteList = mutableListOf<Note>()
    private var listState = "true"
    private var listArrange = "1"

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this,
            NoteViewModelFactory(activity.application)
        )[NoteViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val files: Array<String> = requireContext().fileList()
        if (!files.contains(getString(R.string.list_state))) {
            writeToListStateFile()
        } else {
            listState = requireContext()
                .openFileInput(getString(R.string.list_state))
                .bufferedReader()
                .readLine()
        }

        if (!files.contains(getString(R.string.list_arrange))) {
            writeToFilterFile()
        } else {
            listArrange = requireContext()
                .openFileInput(getString(R.string.list_arrange))
                .bufferedReader()
                .readLine()
        }
    }

    private fun writeToListStateFile() {
        requireContext().openFileOutput(getString(R.string.list_state), Context.MODE_PRIVATE).use {
            it.write(listState.toByteArray())
        }
    }

    private fun writeToFilterFile() {
        requireContext().openFileOutput(getString(R.string.list_arrange), Context.MODE_PRIVATE).use {
            it.write(listArrange.toByteArray())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCategoryRecyclerview()
        setNoteRecyclerview()

        binding.apply {
            textViewSelectedItem.text =
                getString(R.string.selected_item_quantity, selectedNoteList.size)
            buttonAddNote.setOnClickListener {
                actionCreateNote()
            }

            searchView.setOnQueryTextListener(this@NoteFragment)
            imageLayoutSet.setOnClickListener { changeRecyclerLayout() }
            imageFilter.setOnClickListener { openConfirmationDialog() }
            imageCancel.setOnClickListener { cancelSelectMode() }
            imageRemoveSelectedNote.setOnClickListener { submitRemoveNotes() }
            imagePinSelectedNote.setOnClickListener { pinNotes() }
        }
    }

    private fun setCategoryRecyclerview() {
        val adapterCategory = NoteCategoryAdapter { id: Int ->
            categoryId = id
            categoryFilter()
        }
        binding.recyclerViewCategoryNote.adapter = adapterCategory
        sharedViewModel.allCategory.observe(this.viewLifecycleOwner) { categories ->
            categories.let {
                adapterCategory.submitList(it)
            }
        }
    }

    private fun setNoteRecyclerview() {
        adapter = NoteAdapter(this) { note ->
            if (isMultiSelectMode) {
                operateNoteSelected(note)
            } else {
                val action = NoteFragmentDirections.actionNoteFragmentToAddNoteFragment(
                    note.noteId,
                    note.categoryId)
                findNavController().navigate(action)
            }
        }
        binding.recyclerViewListNote.adapter = adapter
        actionSetLayout()
        categoryFilter()
    }

    private fun changeRecyclerLayout() {
        val state = !listState.toBooleanStrict()
        listState = state.toString()
        actionSetLayout()
        writeToListStateFile()
    }

    private fun actionSetLayout() {
        binding.imageLayoutSet.setImageResource(
            if (listState.toBooleanStrict()) R.drawable.icon_linear_layout
            else R.drawable.icon_grid_layout
        )
        binding.recyclerViewListNote.layoutManager =
            if (listState.toBooleanStrict()) {
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else {
                LinearLayoutManager(requireContext())
            }
    }

    private fun openConfirmationDialog() {
        val options = resources.getStringArray(R.array.array_option_sort)
        var checkedItem = listArrange.toInt()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.sort_by))
            .setSingleChoiceItems(options, checkedItem) { _, which ->
                checkedItem = which
            }
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                listArrange = checkedItem.toString()
                writeToFilterFile()
                arrangeNotes()
                adapter.submitList(listNote.toList())
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
            .show()
    }

    private fun actionCreateNote() {
        val newNote = Note()
        sharedViewModel.insertNote(newNote)
        val action =
            NoteFragmentDirections.actionNoteFragmentToAddNoteFragment(categoryId = newNote.categoryId)
        findNavController().navigate(action)
    }

    private fun changeToSelectMode() {
        isMultiSelectMode = true
        binding.layoutSelectMulti.visibility = View.VISIBLE
        binding.layoutNormal.visibility = View.GONE
        binding.recyclerViewCategoryNote.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun cancelSelectMode() {
        isMultiSelectMode = false
        binding.layoutSelectMulti.visibility = View.GONE
        binding.layoutNormal.visibility = View.VISIBLE
        binding.recyclerViewCategoryNote.visibility = View.VISIBLE
        adapter.notifyDataSetChanged()
    }

    // check whether any note selected
    private fun submitRemoveNotes() {
        if (selectedNoteList.isNotEmpty()) {
            submitDialog()
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.no_item_selected_remind), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun submitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.submit_remove_notes))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                removeNote()
            }
            .setNegativeButton(getString(R.string.no_thanks)) { _, _ -> }
            .show()
    }

    private fun removeNote() {
        selectedNoteList.forEach {
            sharedViewModel.deleteNote(it)
        }
        selectedNoteList.clear()
        binding.textViewSelectedItem.text =
            getString(R.string.selected_item_quantity, selectedNoteList.size)
    }

    private fun actionPinNotes() {
        selectedNoteList.forEach { note ->
            note.isPinned = !note.isPinned
            sharedViewModel.updateNote(note)
        }
        selectedNoteList.clear()
    }

    private fun pinNotes() {
        if (selectedNoteList.isNotEmpty()) {
            actionPinNotes()
        } else {
            Toast.makeText(
                requireContext(), getString(R.string.no_item_selected_remind), Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun operateNoteSelected(note: Note) {
        if ( selectedNoteList.find {
                it.noteId == note.noteId
        } == null ) {
            selectedNoteList.add(note)
        } else {
            selectedNoteList.remove(note)
        }
        binding.textViewSelectedItem.text =
            getString(R.string.selected_item_quantity, selectedNoteList.size)
    }

    private fun categoryFilter() {
        when (categoryId) {
            1 -> {
                val action = NoteFragmentDirections.actionNoteFragmentToAddCategoryFragment()
                findNavController().navigate(action)
            }
            2 -> {
                sharedViewModel.allNotes.observe(viewLifecycleOwner, {
                    listNote = it.toMutableList()
                    arrangeNotes()
                    adapter.submitList(listNote)
                })
            }
            else -> {
                sharedViewModel.getNoteByCategory(categoryId).observe(viewLifecycleOwner, {
                    listNote = it.toMutableList()
                    arrangeNotes()
                    adapter.submitList(listNote)
                })
            }
        }
    }

    private fun arrangeNotes() {
        when (listArrange.toInt()) {
            0 -> listNote.sortBy { it.dueDate }
            1 -> listNote.sortByDescending { it.dueDate }
            2 -> listNote.sortBy { it.noteTitle.first() }
            3 -> listNote.sortByDescending { it.noteTitle.first() }
        }
        arrangePin()
    }

    // arrange pinned notes
    private fun arrangePin() {
        val newList = mutableListOf<Note>()
        var i = 0
        while (i < listNote.size) {
            if (listNote[i].isPinned) {
                newList.add(listNote[i])
                listNote.removeAt(i)
            } else
                i += 1
        }
        listNote.addAll(0, newList)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            search(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            search(query)
        }
        return true
    }

    private fun search(query: String) {
        val nameQuery = "%$query%"
        sharedViewModel.searchDatabase(nameQuery).observe(this, { notes ->
            notes.let {
                listNote = it.toMutableList()
                arrangePin()
                adapter.submitList(listNote)
            }
        })
    }
}