package com.example.wasifnadeem.maidan_android

import com.example.wasifnadeem.maidan_android.models.User


data class ApiResponse(private var statusCode: Number, private var statusMessage: String,
                       private var payload: User, private var message: String) {

    //Getters
    fun getStatusCode(): Number {return this.statusCode}
    fun getStatusMessage(): String {return this.statusMessage}
    fun getPayload(): User {return this.payload}
    fun getMessage(): String {return this.message}

    //Setters
    fun setStatusCode(statusCode: Number) {this.statusCode = statusCode}
    fun setStatusMessage(message: String){this.statusMessage = message}
    fun setPayload(payload: User){this.payload = payload}
    fun setMessage(message: String){this.message = message}

}