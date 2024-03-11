package com.example.testmobsec

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

    data object ProfileScreen: Screen(route = "profile_screen")

    data object SearchScreen: Screen(route = "search_screen")
    data object UploadScreen: Screen(route = "upload_screen")
    data object EditProfileScreen: Screen(route = "edit_profile_screen")

    data object PostScreen: Screen(route = "post_screen")
    data object CommentScreen: Screen(route = "comment_screen/{postId}"){
    }

    data object PostDetailsScreen: Screen(route = "postDetails_screen/{postId}"){

    }

    data object OthersProfileScreen: Screen(route = "othersProfile_screen/{userId}"){

    }
}

@Composable
fun NavGraph(
    navController: NavHostController,sharedViewModel: SharedViewModel

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
        composable(
            route = Screen.CommentScreen.route, // Use the updated route with argument
            arguments = listOf(navArgument("postId") { type = NavType.StringType }) // Define the argument
        ) { backStackEntry ->
            // Extract the postId argument
            val postId = backStackEntry.arguments?.getString("postId") ?: throw IllegalStateException("postId must be provided")

            CommentScreen(
                navController = navController,
                postId = postId // Pass the postId to CommentScreen
            )
        }

        composable(
            route = Screen.PostDetailsScreen.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract postId
            val postId = backStackEntry.arguments?.getString("postId") ?: throw IllegalStateException("postId must be provided")
            PostDetailsScreen(navController = navController, postId)
            // Implement your PostDetailsScreen composable, which shows the post details
        }

        composable(
            route = Screen.OthersProfileScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract postId
            val userId = backStackEntry.arguments?.getString("userId") ?: throw IllegalStateException("userId must be provided")
            OthersProfileScreen(navController = navController, userId =  userId)
            // Implement your PostDetailsScreen composable, which shows the post details
        }


    }
}
