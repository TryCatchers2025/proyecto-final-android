package com.trycatchers.hotel.compose.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.KingBed
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.trycatchers.hotel.compose.Screen

data class NavItem(val label: String, val icon: ImageVector, val screen: Screen)

@Composable
fun BottomBar(navigateTo: (Screen) -> Unit, currentRoute: String) {
    val items =
        listOf(
            NavItem("Inicio", Icons.Outlined.Search, Screen.InitialSearch),
            NavItem("Habitaciones", Icons.Outlined.KingBed, Screen.RoomCatalog),
            NavItem("Cuenta", Icons.Outlined.AccountCircle, Screen.UserAccount),
        )

    BottomBarView(items = items, currentRoute = currentRoute, navigateTo = { navigateTo(it) })
}

@Composable
fun BottomBarView(items: List<NavItem>, currentRoute: String?, navigateTo: (Screen) -> Unit) {
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    NavigationBar(
        containerColor =
            if (isDarkTheme) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primaryContainer,
        contentColor =
            if (isDarkTheme) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        for (item in items) {
            val isSelected =
                when (item.screen) {
                    Screen.RoomFinder ->
                        item.screen.matches(currentRoute) ||
                                Screen.InitialSearch.matches(currentRoute)

                    else -> item.screen.matches(currentRoute)
                }
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label =
                    if (isSelected) {
                        { Text(item.label) }
                    } else {
                        null
                    },
                selected = isSelected,
                onClick = { navigateTo(item.screen) },
                colors =
                    NavigationBarItemColors(
                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                        selectedTextColor =
                            if (isDarkTheme)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor =
                            if (isDarkTheme)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.secondary,
                        unselectedTextColor =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIconColor =
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.38f
                            ),
                        disabledTextColor =
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.38f
                            )
                    )
            )
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    val items =
        listOf(
            NavItem("Inicio", Icons.Outlined.Search, Screen.RoomFinder),
            NavItem("Habitaciones", Icons.Outlined.KingBed, Screen.RoomCatalog),
            NavItem("Cuenta", Icons.Outlined.AccountCircle, Screen.UserAccount),
        )

    BottomBarView(items = items, currentRoute = Screen.RoomFinder.route, navigateTo = {})
}
