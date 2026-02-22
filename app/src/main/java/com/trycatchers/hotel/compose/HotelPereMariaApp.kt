package com.trycatchers.hotel.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trycatchers.hotel.compose.components.navigation.BottomBar
import com.trycatchers.hotel.compose.components.navigation.TopBar
import com.trycatchers.hotel.compose.screens.*
import com.trycatchers.hotel.viewmodels.RoomFinderViewModel

/**
 * Composable raíz de la aplicación.
 *
 * Configura el scaffold principal con TopBar y BottomBar condicionales, y delega la navegación a
 * [HotelPereMariaNavHost].
 */
@Composable
fun HotelPereMariaApp(onThemeToggle: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute: String = navBackStackEntry?.destination?.route ?: ""

    val currentScreen = Screen.fromRoute(currentRoute)
    val showTopBar = currentScreen?.showTopBar ?: true
    val showBottomBar = currentScreen?.showBottomBar ?: true

    fun navigateTo(screen: Screen, route: String? = null, popToRoot: Boolean = false) {
        val destination = route ?: screen.route
        navController.navigate(destination) {
            if (popToRoot) {
                popUpTo(Screen.InitialSearch.route) { inclusive = false }
            }
            launchSingleTop = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                TopBar(
                    navigateTo = { screen, route ->
                        navigateTo(screen, route, popToRoot = false)
                    },
                    onThemeToggle = onThemeToggle
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(
                    navigateTo = { screen -> navigateTo(screen, popToRoot = true) },
                    currentRoute = currentRoute
                )
            }
        }
    ) { innerPadding ->
        HotelPereMariaNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            navigateTo = ::navigateTo
        )
    }
}

/** NavHost que registra todas las rutas de la aplicación. */
@Composable
fun HotelPereMariaNavHost(
    navController: NavHostController,
    modifier: Modifier,
    navigateTo: (Screen, String?, Boolean) -> Unit,
) {
    fun navigateToUserAccountFromBookingFlow() {
        navController.navigate(Screen.UserAccount.route) {
            popUpTo(Screen.InitialSearch.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.InitialSearch.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.InitialSearch.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.InitialSearch.route) { backStackEntry ->
            val sharedRoomFinderViewModel: RoomFinderViewModel = hiltViewModel(backStackEntry)

            InitialSearchScreen(
                onNavigateToFilters = {
                    navController.navigate(Screen.RoomFinder.route) { launchSingleTop = true }
                },
                viewModel = sharedRoomFinderViewModel
            )
        }

        composable(route = Screen.RoomFinder.route) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InitialSearch.route)
                }
            val sharedRoomFinderViewModel: RoomFinderViewModel = hiltViewModel(parentEntry)

            RoomFinderScreen(
                onNavigateBack = {
                    sharedRoomFinderViewModel.reset()
                    navController.popBackStack()
                },
                onNavigateToSelector = {
                    navController.navigate(Screen.RoomSelector.route) { launchSingleTop = true }
                },
                viewModel = sharedRoomFinderViewModel
            )
        }

        composable(route = Screen.RoomSelector.route) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InitialSearch.route)
                }
            val sharedRoomFinderViewModel: RoomFinderViewModel = hiltViewModel(parentEntry)

            RoomSelectorScreen(
                onNavigateBack = { navController.popBackStack() },
                navigateToRoomDetails = { roomId ->
                    navigateTo(
                        Screen.RoomDetails,
                        Screen.RoomDetails.createRoute(roomId),
                        false
                    )
                },
                navigateToBookingSummary = { roomId, start, end, occupants ->
                    val route =
                        Screen.BookingSummary.createRoute(
                            roomId = roomId,
                            startDateMillis = start,
                            endDateMillis = end,
                            occupants = occupants
                        )
                    navController.navigate(route)
                },
                viewModel = sharedRoomFinderViewModel
            )
        }

        composable(route = Screen.RoomCatalog.route) {
            RoomCatalogScreen(
                navigateToRoomDetails = { roomId ->
                    navigateTo(
                        Screen.RoomDetails,
                        Screen.RoomDetails.createRoute(roomId),
                        false
                    )
                }
            )
        }

        composable(route = Screen.UserAccount.route) {
            UserAccountScreen(
                onNavigateToBookingDetail = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.UserProfile.route)
                }
            )
        }

        composable(route = Screen.UserProfile.route) {
            UserProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.RoomDetails.route) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InitialSearch.route)
                }
            val sharedRoomFinderViewModel: RoomFinderViewModel = hiltViewModel(parentEntry)

            RoomDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onBookRoom = {
                    sharedRoomFinderViewModel.reset()
                    navController.navigate(Screen.InitialSearch.route) {
                        popUpTo(Screen.InitialSearch.route) { inclusive = true }
                    }
                }
            )
        }

        composable(route = Screen.BookingSummary.route) { backStackEntry ->
            val parentEntry =
                remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InitialSearch.route)
                }
            val sharedRoomFinderViewModel: RoomFinderViewModel = hiltViewModel(parentEntry)

            BookingSummaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPayment = { bookingId ->
                    sharedRoomFinderViewModel.reset()
                    navController.navigate(Screen.BookingPayment.createRoute(bookingId))
                },
                onNavigateToUserAccount = {
                    sharedRoomFinderViewModel.reset()
                    navigateToUserAccountFromBookingFlow()
                }
            )
        }

        composable(route = Screen.BookingPayment.route) {
            BookingPaymentScreen(
                onNavigateBack = { navigateToUserAccountFromBookingFlow() },
                onFinish = { navigateToUserAccountFromBookingFlow() }
            )
        }

        composable(route = Screen.BookingDetail.route) {
            BookingDetailScreen(
                onNavigateBack = { navigateToUserAccountFromBookingFlow() },
                onNavigateToPayment = { bookingId ->
                    navController.navigate(Screen.BookingPayment.createRoute(bookingId))
                }
            )
        }
    }
}
