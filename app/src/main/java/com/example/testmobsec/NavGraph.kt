package com.example.testmobsec

import PostScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.testmobsec.util.SharedViewModel




sealed class Screen(val route:String){


    data object RegisterScreen: Screen(route = "register_screen")

    data object LoginScreen: Screen(route = "login_screen")
    data object HomeScreen: Screen(route = "home_screen")

    data object ProfileScreen: Screen(route = "profile_screen")

    data object SearchScreen: Screen(route = "search_screen")
    data object UploadScreen: Screen(route = "upload_screen")
    data object EditProfileScreen: Screen(route = "edit_profile_screen")

    data object PostScreen: Screen(route = "post_screen")
}

@Composable
fun NavGraph(
    navController: NavHostController,sharedViewModel: SharedViewModel

) {


    NavHost(
        navController = navController,
        startDestination = Screen.RegisterScreen.route,

        ){
//
        composable(Screen.RegisterScreen.route){
            RegisterScreen(
//
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(Screen.LoginScreen.route){
            LoginScreen(
                navController = navController
            )
        }
        composable(Screen.HomeScreen.route){
            HomeScreen(
                navController = navController
            )
        }
        composable(Screen.ProfileScreen.route){
            ProfileScreen(
                navController = navController
            )
        }
        composable(Screen.UploadScreen.route){
            UploadScreen(
                navController = navController
            )
        }
        composable(Screen.SearchScreen.route){
            SearchScreen(
                navController = navController
            )
        }
        composable(Screen.EditProfileScreen.route){
            EditProfileScreen(
                navController = navController
            )
        }
        composable(Screen.PostScreen.route){
            PostScreen(
                navController = navController
            )
        }

    }
}
