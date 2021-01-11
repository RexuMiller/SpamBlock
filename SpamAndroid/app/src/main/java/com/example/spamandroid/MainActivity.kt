package com.example.spamandroid

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class MainActivity : AppCompatActivity() {

    private val requestReceiveSms = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                requestReceiveSms
            )
        }
    }

    override fun onStart(){
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSmsReceived(event: OnReceiverEvent) {
        message_text.text = event.send_sms
        phone.text = event.phone_number
        spamFound()
    }

    private fun spamFound(){
        val classifier = Classifier(this, "word_dict.json")
        classifier.setMaxLength(171)
        classifier.setCallback(object : Classifier.DataCallback {
            @SuppressLint("SetTextI18n")
            override fun onDataProcessed(result: HashMap<String, Int>?) {
                val message = message_text.text.toString().toLowerCase(Locale.ROOT).trim()
                if (!TextUtils.isEmpty(message)) {
                    classifier.setVocab(result)
                    val tokenizedMessage = classifier.tokenize(message)
                    val paddedMessage = classifier.padSequence(tokenizedMessage)
                    val results = classifySequence(paddedMessage)
                    val class1 = results[0]
                    val class2 = results[1]
                    val class1_str = class1.toString()
                    val class2_str = class2.toString()
                    val scale = Math.pow(10.0, 3.0)
                    val i = Math.ceil(class1_str.toDouble()*scale)/scale
                    val j = Math.ceil(class2_str.toDouble()*scale)/scale
                    if (j<0.02){
                        result_text.text = "Не спам"
                    }else{
                        result_text.text = "Спам"
                    }


                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter a message.",
                        Toast.LENGTH_LONG
                    ).show();
                }

            }
        })
        classifier.loadData()
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val MODEL_ASSETS_PATH = "model.tflite"
        val assetFileDescriptor = assets.openFd(MODEL_ASSETS_PATH)
        val fileInputStream = FileInputStream(assetFileDescriptor.getFileDescriptor())
        val fileChannel = fileInputStream.getChannel()
        val startoffset = assetFileDescriptor.getStartOffset()
        val declaredLength = assetFileDescriptor.getDeclaredLength()
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength)
    }

    fun classifySequence(sequence: IntArray): FloatArray {
        val interpreter = Interpreter(loadModelFile())
        val inputs: Array<FloatArray> = arrayOf(sequence.map { it.toFloat() }.toFloatArray())
        val outputs: Array<FloatArray> = arrayOf(floatArrayOf(0.0f, 0.0f))
        interpreter.run(inputs, outputs)
        return outputs[0]
    }
}
