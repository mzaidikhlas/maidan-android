package com.maidan.android.client.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.maidan.android.client.R
import com.maidan.android.client.models.Booking
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.util.ArrayList

class BookingScheduleAdapter(private val bookingSchedule: ArrayList<Booking>) : RecyclerView.Adapter<BookingScheduleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_bookings_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bookingSchedule.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookingItem: Booking = bookingSchedule[position]
        if(bookingItem.getVenue().getPictures() != null){
            Picasso.get().load(bookingItem.getVenue().getPictures()!![0]).into(holder.image)
        }else{
            Picasso.get().load(R.drawable.like).into(holder.image)
        }

        holder.venue.text = bookingItem.getVenue().getName()
        holder.date.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(bookingItem.getFrom())
        holder.time.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(bookingItem.getFrom())
        holder.statusBooking.text  = bookingItem.getStatus()
        holder.amount.text = bookingItem.getTransaction()!!.getTotal().toString()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById(R.id.image) as ImageView
        val date = itemView.findViewById(R.id.bookingDate) as TextView
        val time = itemView.findViewById(R.id.bookingTime) as TextView
        val venue = itemView.findViewById(R.id.venuePlace) as TextView
        val statusBooking = itemView.findViewById(R.id.statusBooking) as TextView
        val amount = itemView.findViewById(R.id.amount) as TextView

    }
}