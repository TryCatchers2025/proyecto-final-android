package com.trycatchers.hotel.compose

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Define todas las pantallas de la aplicación con sus rutas de navegación y argumentos.
 */
sealed class Screen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList(),
    val showTopBar: Boolean = true,
    val showBottomBar: Boolean = true,
) {
    val baseRoute: String
        get() = route.substringBefore("/{").substringBefore("?")

    fun matches(route: String?): Boolean = route?.startsWith(baseRoute) == true

    data object InitialSearch :
        Screen(route = "initial-search", showTopBar = false, showBottomBar = true)

    data object RoomFinder :
        Screen(route = "room-finder", showTopBar = false, showBottomBar = false)

    data object RoomSelector :
        Screen(route = "room-selector", showTopBar = false, showBottomBar = false)

    data object RoomCatalog : Screen("room-catalog")
    data object UserAccount : Screen("user-account")
    data object UserProfile : Screen("user-profile")

    data object Login : Screen("login-menu", showBottomBar = false)
    data object Register : Screen("register", showBottomBar = false)
    data object RoomDetails :
        Screen(
            "room-details/{roomId}",
            listOf(navArgument(name = "roomId") { type = NavType.StringType }),
            showTopBar = false
        ) {
        fun createRoute(roomId: String) = "room-details/$roomId"
    }

    data object BookingSummary :
        Screen(
            "booking-summary/{roomId}?startDateMillis={startDateMillis}&endDateMillis={endDateMillis}&occupants={occupants}",
            listOf(
                navArgument(name = "roomId") { type = NavType.StringType },
                navArgument(name = "startDateMillis") { type = NavType.LongType },
                navArgument(name = "endDateMillis") { type = NavType.LongType },
                navArgument(name = "occupants") { type = NavType.IntType }
            ),
            showTopBar = false,
            showBottomBar = false
        ) {
        fun createRoute(
            roomId: String,
            startDateMillis: Long,
            endDateMillis: Long,
            occupants: Int,
        ): String =
            "booking-summary/$roomId?startDateMillis=$startDateMillis&endDateMillis=$endDateMillis&occupants=$occupants"
    }

    data object BookingPayment :
        Screen(
            "booking-payment/{bookingId}",
            listOf(navArgument(name = "bookingId") { type = NavType.StringType }),
            showTopBar = false,
            showBottomBar = false
        ) {
        fun createRoute(bookingId: String) = "booking-payment/$bookingId"
    }

    data object BookingDetail :
        Screen(
            "booking-detail/{bookingId}",
            listOf(navArgument(name = "bookingId") { type = NavType.StringType }),
            showTopBar = false,
            showBottomBar = false
        ) {
        fun createRoute(bookingId: String) = "booking-detail/$bookingId"
    }

    companion object {
        val all: List<Screen> =
            listOf(
                InitialSearch,
                RoomFinder,
                RoomSelector,
                RoomCatalog,
                UserAccount,
                RoomDetails,
                BookingSummary,
                BookingPayment,
                BookingDetail,
                Login,
                UserProfile,
                Register,
            )

        fun fromRoute(route: String?): Screen? = all.firstOrNull { it.matches(route) }
    }
}
