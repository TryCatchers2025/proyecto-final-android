package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatIndividualSuite
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trycatchers.hotel.compose.components.rooms.RoomCard
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.viewmodels.RoomCatalogViewModel
import okhttp3.internal.connection.ExchangeFinder

@Composable
fun RoomCatalogScreen(navigateToRoomDetails: (String) -> Unit) {
    val roomCatalogViewModel: RoomCatalogViewModel = hiltViewModel()

    RoomCatalogView(
        navigateToRoomDetails = navigateToRoomDetails,
    )
}

@Composable
fun RoomCatalogView(
    navigateToRoomDetails: (String) -> Unit,
    viewModel: RoomCatalogViewModel = hiltViewModel()
) {
    Box(){
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = "Filter",
                modifier = Modifier
                    .size(40.dp)
                    .padding(10.dp)
                    .clickable{ }

            )
            val rooms by viewModel.rooms.collectAsState()

                LazyColumn {
                    items(3) { room ->
                        RoomCard(room = Room(
                            name = "Room 1",
                            type = "Single",
                            number = 101,
                            price = 50.00,
                            occupied = false,
                            limit = 2,
                            offer = 0,
                            description = "This is a room"

                        )
                        ){
                                clickedRoom ->
                            navigateToRoomDetails(clickedRoom.roomId ?: "123")
                        }
                    }
                }
            }
        }


}

@Preview(showSystemUi = true)
@Composable
fun RoomCatalogPreview() {
    Box(modifier = Modifier.fillMaxSize()) { RoomCatalogView({}) }
}
