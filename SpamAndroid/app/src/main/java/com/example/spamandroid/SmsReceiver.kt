package com.example.spamandroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast
import org.greenrobot.eventbus.EventBus


class OnReceiverEvent(messageText: String, phone: String) {
     var send_sms : String = messageText
     var phone_number: String = phone
    }

class SmsReceiver : BroadcastReceiver() {
    var messageText : String = ""
    var phoneNumber : String = ""

    override fun onReceive(context: Context?, intent: Intent) {
        val extras = intent.extras
        if (extras != null){
            val sms = extras.get("pdus") as Array<Any>

            for (i in sms.indices){
                val format = extras.getString("format")

                var smsMessage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    SmsMessage.createFromPdu(sms[i] as ByteArray, format)
                }else{
                    SmsMessage.createFromPdu(sms[i] as ByteArray)
                }
                phoneNumber = smsMessage.originatingAddress.toString()
                messageText = smsMessage.messageBody.toString()
                EventBus.getDefault().post(OnReceiverEvent(messageText,phoneNumber));
            }
        }
    }
}

