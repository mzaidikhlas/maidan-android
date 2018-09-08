package com.maidan.android.client.adapter

import android.content.res.Resources
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.maidan.android.client.R
import com.maidan.android.client.models.Category
import com.squareup.picasso.Picasso
import java.util.ArrayList
import kotlin.coroutines.experimental.coroutineContext

class CategoryRecyclerviewAdapter(val categoryList: ArrayList<Category>) : RecyclerView.Adapter<CategoryRecyclerviewAdapter.ViewHolder>() {

    private var ROW_INDEX = -1
    private lateinit var image: ImageView
    private var selectedItem  = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_category, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val categoryItem: Category = categoryList[position];
        holder.textViewName.text = categoryItem.name
        Log.d("TAG",position.toString())

        if (categoryItem.imageView != null)
        {
            Picasso.get().load(categoryItem.imageView!!).into(holder.imageViewIcon)
        } else
        {
            Picasso.get().load(R.drawable.like).into(holder.imageViewIcon)
        }

        holder.itemlayout.setOnClickListener {
            ROW_INDEX = holder.adapterPosition
            notifyDataSetChanged()
        }

        if (ROW_INDEX == holder.adapterPosition) {
            holder.itemlayout.setBackgroundResource(R.drawable.gradient)
            holder.textViewName.setTextColor(Color.parseColor("#FFFFFF"))

        }
        else {
            holder.itemlayout.setBackgroundResource(R.drawable.unselected_item)
            holder.textViewName.setTextColor(Color.parseColor("#000000"))
            //  holder.textViewName.setTextColor(Color.parseColor("#FFFFFF"))
        }
        if (ROW_INDEX == -1){
            if ((categoryList.size/2) == holder.adapterPosition) {
                Log.d("Adapter: Init", " ${(categoryList.size/2)} - ${holder.adapterPosition}")
                holder.itemlayout.setBackgroundResource(R.drawable.gradient)
                holder.textViewName.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName = itemView.findViewById(R.id.categoryText) as TextView;
        val imageViewIcon = itemView.findViewById(R.id.categoryIcon) as ImageView;
        val itemlayout = itemView.findViewById(R.id.categoryCardlayout) as ConstraintLayout
    }

}
