package com.example.testmobsec.util

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SharedViewModel(): ViewModel() {
    private var storage = FirebaseStorage.getInstance()

    fun saveData(
        userData: UserData,
        imageData: ByteArray,
        context: Context
    ) = CoroutineScope(Dispatchers.Main).launch{

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid // Handle the case where no user is signed in

        val fireStoreRef = Firebase.firestore
            .collection("users")
            .document(uid!!)

        try {
            fireStoreRef.set(userData).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Successfully saved data", Toast.LENGTH_SHORT).show()
            }
        } catch(e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
        }



    }
    fun uploadDefaultProfileImage(userId: String, imageData: ByteArray, context: Context) {
        val storageRef = storage.reference.child("images/$userId/profile_picture.jpg")

        // Upload the image data to Firebase Storage
        storageRef.putBytes(imageData)
            .addOnSuccessListener {

//                // Image upload successful, invoke callback with success
//                callback.onResult(true, "User and profile image created successfully")

            }
            .addOnFailureListener { e ->
                // Image upload failed, invoke callback with failure
//                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
//                callback.onResult(false, "Failed to upload profile image: ${e.message}")
            }
    }

    interface AuthResultCallBack {
        fun onResult(success: Boolean, message: String)
    }




}