package com.example.noteandschedule.ui.note

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.example.noteandschedule.R
import com.example.noteandschedule.adapter.note.ImageViewPagerAdapter
import com.example.noteandschedule.data.entity.Note
import com.example.noteandschedule.databinding.FragmentImageBinding
import com.example.noteandschedule.viewmodel.NoteViewModel
import com.example.noteandschedule.viewmodel.NoteViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding
    private val navArgs : ImageFragmentArgs by navArgs()
    private lateinit var note: Note

    private val sharedViewModel: NoteViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this, NoteViewModelFactory(activity.application))[NoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = navArgs.noteId
        sharedViewModel.getNoteInfo(noteId).observe(this.viewLifecycleOwner) {
            note = it
            onBind()
        }

    }

    private fun onBind() {
        val images = note.noteImage.toTypedArray()
        val adapter = ImageViewPagerAdapter(images)
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(navArgs.currentPosition, false)
        binding.imageClose.setOnClickListener { backToBeforeFragment() }
        binding.imageDelete.setOnClickListener { openDialogDelete(binding.viewPager.currentItem) }
        binding.textViewImageQuantity.text =
            resources.getString(R.string.image_order, binding.viewPager.currentItem, images.size)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.textViewImageQuantity.text =
                    resources.getString(R.string.image_order, position+1, images.size)
            }
        })
    }

    private fun openDialogDelete(currentItem: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_picture))
            .setMessage(getString(R.string.verify_delete_question))
            .setPositiveButton(getString(R.string.ok)) {_, _ ->
                deleteImage(currentItem)
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }
            .show()
    }

    private fun deleteImage(currentItem: Int) {
        note.noteImage.removeAt(currentItem)
        sharedViewModel.updateNote(note)
        if (note.noteImage.isEmpty()) {
            backToBeforeFragment()
        }
    }

    private fun backToBeforeFragment() {
        findNavController().navigateUp()
    }

}