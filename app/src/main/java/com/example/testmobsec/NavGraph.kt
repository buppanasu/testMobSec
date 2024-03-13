package com.example.testmobsec

import BandScreen
import BandViewModel
import PostScreen
import SearchBandScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.testmobsec.util.SharedViewModel




sealed class Screen(val route:String){


    data object RegisterScreen: Screen(route = "register_screen")

    data object LoginScreen: Screen(route = "login_screen")
    data object HomeScreen: Screen(route = "home_screen")

    data object CreateOrJoinBandScreen: Screen(route = "createorjoinband_screen")

    data object CreateBandScreen: Screen(route = "createband_screen")
    data object JoinBandScreen: Screen(route = "joinband_screen")
    data object SearchBandScreen: Screen(route = "searchband_screen")
    data object BandScreen: Screen(route = "band_screen")

    data object ProfileScreen: Screen(route = "profile_screen")

    data object SearchScreen: Screen(route = "search_screen")
    data object UploadScreen: Screen(route = "upload_screen")
    data object EditProfileScreen: Screen(route = "edit_profile_screen")
    data object OtherBandScreen: Screen(route = "otherband_screen")

    data object PostScreen: Screen(route = "post_screen")
}

@Composable
fun NavGraph(
    navController: NavHostController,sharedViewModel: SharedViewModel, bandViewModel: BandViewModel

) {


    NavHost(
        navController = navController,
        startDestination = Screen.LoginScreen.route,

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
        composable(Screen.CreateOrJoinBandScreen.route){
            CreateOrJoinBandScreen(
                navController = navController
            )
        }
        composable(Screen.CreateBandScreen.route){
            CreateBandScreen(
                navController = navController
            )
        }
        composable(Screen.BandScreen.route){
            BandScreen(
                navController = navController
            )
        }

        composable(
            "other_band_screen/{bandId}",
            arguments = listOf(navArgument("bandId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bandId = backStackEntry.arguments?.getString("bandId") ?: return@composable
            OtherBandScreen(navController, bandId, bandViewModel)
        }

        composable(Screen.JoinBandScreen.route){
            JoinBandScreen(
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
        composable(Screen.SearchBandScreen.route){
            SearchBandScreen(
                navController = navController
            )
        }

    }
}
