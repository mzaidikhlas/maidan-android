package com.maidan.android.client.models

data class Venue(private var name: String, private var location: Location, private var pictures: ArrayList<String>,
                 private var verified: Boolean, private var amenities: Amenities, private var reviews: Reviews, private var owner: User,
                 private var activityType: String, private var rate: Rate, private var minBookingHour: Int,
                 private var createdAt: String, private var updatedAt: String) {

    //Getters
    fun getName(): String{return this.name}
    fun getLocation(): Location {return this.location}
    fun getPictures(): ArrayList<String>{return this.pictures}
    fun getVerifies(): Boolean{return this.verified}
    fun getAmenities(): Amenities {return this.amenities}
    fun getReviews(): Reviews {return this.reviews}
    fun getOwner(): User {return this.owner}
    fun getActivityType(): String{return this.activityType}
    fun getRate(): Rate {return this.rate}
    fun getMinBookingHour(): Int{return this.minBookingHour}
    fun getCreatedAt(): String {return this.createdAt}
    fun getUpdatedAt(): String {return this.updatedAt}

    //Setters
    fun setName(name: String){this.name = name}
    fun setLocation(location: Location){this.location = location}
    fun setPictures(pictures: ArrayList<String>){this.pictures = pictures}
    fun setVerifies(verified: Boolean){this.verified = verified}
    fun setAmenities(amenities: Amenities){this.amenities = amenities}
    fun setReviews(reviews: Reviews){this.reviews = reviews}
    fun setOwner(owner: User){this.owner = owner}
    fun setActivityType(activityType: String){this.activityType = activityType}
    fun setRate(rate: Rate){this.rate = rate}
    fun setMinBookingHour(minBookingHour: Int){this.minBookingHour = minBookingHour}
    fun setCreatedAt(createdAt: String){this.createdAt = createdAt}
    fun setUpdatedAt(updatedAt: String){this.updatedAt = updatedAt}

}