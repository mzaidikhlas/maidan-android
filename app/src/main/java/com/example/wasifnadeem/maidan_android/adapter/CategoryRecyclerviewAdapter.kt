package com.example.wasifnadeem.maidan_android.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.wasifnadeem.maidan_android.R
import com.example.wasifnadeem.maidan_android.models.Category
import kotlinx.android.synthetic.main.booking_category.*
import java.util.ArrayList

class CategoryRecyclerviewAdapter(val categoryList: ArrayList<Category>) : RecyclerView.Adapter<CategoryRecyclerviewAdapter.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_category, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryItem: Category = categoryList[position];
        holder.textViewName.text = categoryItem.name;
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewName = itemView.findViewById(R.id.categoryTitle) as TextView ;
        val imageViewIcon = itemView.findViewById(R.id.categoryIcon) as ImageView;
    }
}