package com.example.testmobsec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

import com.example.testmobsec.util.SharedViewModel
import com.example.testmobsec.viewModel.BandViewModel

class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
    private val bandViewModel: BandViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp(sharedViewModel = sharedViewModel, bandViewModel = bandViewModel)
//            Surface {
////                SearchScreenFunction()
//
//                //OthersProfileScreen()
//                //HomeScreen()
//                RegisterScreen(sharedViewModel = sharedViewModel)
//                //LoginScreen()
//                //RegisterScreen()
//            }


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
