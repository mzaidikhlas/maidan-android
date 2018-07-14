package com.example.wasifnadeem.maidan_android.models

data class Location(private var latitude: Number, private var longitude: Number, private var country: String,
                    private var city: String, private var area: String){

    //Getters
    fun getLatitude(): Number{return this.latitude}
    fun getLongitude(): Number{return this.longitude}
    fun getCountry(): String{return this.country}
    fun getCity(): String{return this.city}
    fun getArea(): String{return this.area}

    //Setters
    fun setLatitude(latitude: Number){this.latitude = latitude}
    fun setLongitude(longitude: Number){this.longitude = longitude}
    fun setCountry(country: String){this.country = country}
    fun setCity(city: String){this.city = city}
    fun setArea(area: String){this.area = area}
}