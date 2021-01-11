package com.example.spamandroid

class Saver {
    var messageText : String? = null
    fun save(sms: String){
        messageText=sms
    }
}