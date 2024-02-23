package com.example.testmobsec

import SearchScreen
import SearchScreenFunction
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface

import com.example.testmobsec.util.SharedViewModel

class MainActivity : ComponentActivity() {

    private val sharedViewModel: SharedViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface {
                SearchScreenFunction()
                //OthersProfileScreen()
                //HomeScreen()
                //RegisterScreen(sharedViewModel = sharedViewModel)
                //LoginScreen()
                //RegisterScreen()
            }


            }
        }
    }


