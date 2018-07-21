package com.maidan.android.client.models

data class Booking(private var venue: Venue, private var transaction: Transaction, private var user: User,
                   private var durationOfBooking: Int, private var startTime: String) {

    //Getters
    fun getVenue(): Venue {return this.venue}
    fun getTransaction(): Transaction {return this.transaction}
    fun getUser(): User {return this.user}
    fun getDurationOfBooking(): Int {return this.durationOfBooking}
    fun getStartTime(): String {return this.startTime}

    //Setters
    fun setVenue(venue: Venue){this.venue = venue}
    fun setTransaction(transaction: Transaction) {this.transaction = transaction}
    fun setUser(user: User) {this.user = user}
    fun setDurationOfBooking(durationOfBooking: Int) {this.durationOfBooking = durationOfBooking}
    fun setStartTime(startTime: String) {this.startTime = startTime}
}