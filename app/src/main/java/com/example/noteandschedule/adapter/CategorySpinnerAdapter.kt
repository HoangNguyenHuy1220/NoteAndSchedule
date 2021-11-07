package com.example.noteandschedule.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.noteandschedule.R
import com.example.noteandschedule.data.entity.Category


class CategorySpinnerAdapter(
    context: Context,
    categories: List<Category>)
    : ArrayAdapter<Category>(context, 0, categories) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  convertView ?: LayoutInflater.from(context).inflate(R.layout.selected_item_spinner, parent, false)
        val category = getItem(position)!!
        view.findViewById<TextView>(R.id.textView_selected_cate).text = category.categoryName

        return view
    }


    @SuppressLint("ResourceAsColor", "UseCompatLoadingForDrawables")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view =  convertView ?: LayoutInflater.from(context).inflate(R.layout.dropdown_item_spinner, parent, false)
        val category = getItem(position)!!
        if (position == 0) {
            view.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.icon_add_category)
            view.findViewById<ImageView>(R.id.icon).visibility = view.visibility
        }
        view.findViewById<TextView>(R.id.textView_category).text = category.categoryName

        return view
    }

}