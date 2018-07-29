package com.maidan.android.client.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.maidan.android.client.R
import com.maidan.android.client.models.Venue
import java.util.ArrayList

class VenueCardAdaptor(private val venues: ArrayList<Venue>) : RecyclerView.Adapter<VenueCardAdaptor.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.venue_card, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return venues.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val venue: Venue = venues[position];
        holder.type.text = venue.getActivityType();
        holder.name.text = venue.getName()
        holder.address.text = venue.getLocation().getArea()
        holder.price.text = venue.getRate().getPerHrRate().toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val type = itemView.findViewById(R.id.type) as TextView;
        val imageViewIcon = itemView.findViewById(R.id.ground) as ImageView;
        val name = itemView.findViewById(R.id.groundname) as TextView;
        val address = itemView.findViewById(R.id.address) as TextView;
        val price = itemView.findViewById(R.id.price) as TextView

    }
}