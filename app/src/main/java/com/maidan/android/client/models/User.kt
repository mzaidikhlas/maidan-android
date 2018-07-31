package com.maidan.android.client.models

import java.io.Serializable

data class User(private var email: String, private var name: String, private var password: String,
                private var phone: Number, private var cnic: Number, private var displayAvatar: String,
                private var userRecord: UserRecord): Serializable {
    fun getEmail(): String {
        return this.email
    }

    fun getName(): String {
        return this.name
    }

    fun getPassword(): String {
        return this.password
    }

    fun getPhone(): Number{
        return this.phone
    }
    fun getCnic(): Number{
        return this.cnic
    }

    fun getDisplayAvatar(): String {
        return this.displayAvatar
    }
    fun getUserRecord(): UserRecord {
        return this.userRecord
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun setName(name: String){
        this.name = name
    }

    fun setPassword(password: String){
        this.password = password
    }

    fun setPhone(phone: Number){
        this.phone = phone
    }
    fun setCnic(cnic: Number){
        this.cnic = cnic
    }

    fun setDisplayAvatar(displayAvatar: String){
        this.displayAvatar = displayAvatar
    }
    fun setUserRecord(userRecord: UserRecord){
        this.userRecord = userRecord
    }

}