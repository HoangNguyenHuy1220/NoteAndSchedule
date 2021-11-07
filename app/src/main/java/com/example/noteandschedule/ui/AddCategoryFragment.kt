package com.example.noteandschedule.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.note.CategoryManagementAdapter
import com.example.noteandschedule.data.entity.Category
import com.example.noteandschedule.databinding.AddItemDialogBinding
import com.example.noteandschedule.databinding.FragmentAddCategoryBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddCategoryFragment : Fragment(){

    private lateinit var binding: FragmentAddCategoryBinding

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
        binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.allCategory.observe(this.viewLifecycleOwner) {
            val adapter = CategoryManagementAdapter { v, category ->
                showPopupMenu(v, category)
            }
            binding.recyclerListCategory.adapter = adapter
            binding.recyclerListCategory.layoutManager = LinearLayoutManager(requireContext())
            adapter.submitList(it)
        }
        binding.buttonAdd.setOnClickListener { openDialog() }
        binding.imageBack.setOnClickListener {
            val action = AddCategoryFragmentDirections.actionAddCategoryFragmentToNoteFragment()
            findNavController().navigate(action)
        }
    }

    private fun showPopupMenu(v: View, category: Category) {
        PopupMenu(requireContext(), v).apply {
            inflate(R.menu.category_action_menu)
            setOnMenuItemClickListener{
                onItemClick(it, category)
            }
            show()
        }
    }

    private fun onItemClick(item: MenuItem, category: Category): Boolean {
        if (item.itemId == R.id.edit) {
            openEditDialog(category)
        } else {
            deleteCategory(category)
        }
        return true
    }

    private fun openEditDialog(category: Category) {
        val dialogBinding = AddItemDialogBinding.inflate(requireActivity().layoutInflater)
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogBinding.editTextItemName.setText(category.categoryName, TextView.BufferType.EDITABLE)
        dialogBinding.editTextItemName.requestFocus()
        showSoftKeyboard(dialogBinding.editTextItemName)
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
            category.categoryName = dialogBinding.editTextItemName.text.toString()
            sharedViewModel.updateCategory(category)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun openDialog() {
        val dialogBinding = AddItemDialogBinding.inflate(requireActivity().layoutInflater)
        val dialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        dialogBinding.editTextItemName.requestFocus()
        showSoftKeyboard(dialogBinding.editTextItemName)
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
            val newCategory = Category(categoryName = dialogBinding.editTextItemName.text.toString())
            sharedViewModel.insertCategory(newCategory)
            dialog.dismiss()
        }
        dialogBinding.textViewCancel.setOnClickListener { dialog.dismiss() }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm: InputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun deleteCategory(it: Category) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.remove_category_verify))
            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                sharedViewModel.deleteCategory(it)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->

            }
            .show()
    }

}