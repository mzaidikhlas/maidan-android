package com.example.wasifnadeem.maidan_android.retrofit

import com.example.wasifnadeem.maidan_android.models.Booking
import com.example.wasifnadeem.maidan_android.models.Transaction
import com.example.wasifnadeem.maidan_android.models.User
import com.example.wasifnadeem.maidan_android.models.Venue

data class PayloadFormat(private var docId: String, private var payloadType: String, private var data: Any ) {
    
    //Getters
    fun getDocId(): String {return this.docId}
    fun getPayloadType(): String {return this.payloadType}
    fun getData(): Any {
        var response:Any? = null
        response = when(this.payloadType) {

            "Booking" -> this.data as Booking
            "User" -> this.data as User
            "Venue" -> this.data as Venue
            "Transaction" -> this.data as Transaction

            else -> this.data

        }
        return response
    }

    //Setters
    fun setDocId(docId: String){this.docId = docId}
    fun setPayloadType(payloadType: String){this.payloadType = payloadType}
    fun setData(data: Any){
        this.data = when(this.payloadType){

            "Booking" -> data as Booking
            "User" -> data as User
            "Venue" -> data as Venue
            "Transaction" -> data as Transaction

            else -> this.data
        }
    }



}