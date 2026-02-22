package com.trycatchers.hotel.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trycatchers.hotel.compose.components.navigation.BottomBar
import com.trycatchers.hotel.compose.components.navigation.TopBar
import com.trycatchers.hotel.compose.screens.RoomCatalogScreen
import com.trycatchers.hotel.compose.screens.RoomDetailsScreen
import com.trycatchers.hotel.compose.screens.RoomFinderScreen
import com.trycatchers.hotel.compose.screens.UserAccountScreen

@Composable
fun HotelPereMariaApp(onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute: String = navBackStackEntry?.destination?.route ?: ""

    val navigateTo: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopBar(navigateTo = navigateTo, onThemeToggle = onThemeToggle) },
        bottomBar = { BottomBar(navigateTo = navigateTo, currentRoute = currentRoute) }
    ) { innerPadding ->
        HotelPereMariaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateTo = navigateTo
        )
    }
}

@Composable
fun HotelPereMariaNavHost(
    navController: NavHostController,
    modifier: Modifier,
    navigateTo: (String) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RoomFinder.route,
        modifier = modifier
    ) {
        composable(route = Screen.RoomFinder.route) {
            RoomFinderScreen(navigateToCreateBooking = { roomId ->
                navigateTo(
                    Screen.RoomDetails.createRoute(roomId)
                )
            })
        }

        composable(route = Screen.RoomCatalog.route) {
            RoomCatalogScreen(navigateToRoomDetails = { roomId ->
                navigateTo(
                    Screen.RoomDetails.createRoute(roomId)
                )


            })
        }

        composable(route = Screen.UserAccount.route) { UserAccountScreen() }

        composable(route = Screen.RoomDetails.route){
            RoomDetailsScreen()
        }
    }
}
