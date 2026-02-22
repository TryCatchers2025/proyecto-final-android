package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatIndividualSuite
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.size.Size
import com.trycatchers.hotel.R
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.viewmodels.RoomDetailsViewModel

@Composable
fun RoomDetailsScreen(
    roomDetailsViewModel: RoomDetailsViewModel = hiltViewModel()
) {
    val room = roomDetailsViewModel.room

    if (room == null) {
        RoomDetailsView(room = Room(
            name = "Room 1", type = "Single", number = 101, price = 100.00, occupied = false, limit = 2, offer = 0, description = "This is a room"
        ))
    } else {
        Text("Cargando...")
    }
}

@Composable
fun RoomDetailsView(room: Room) {
    Box(modifier = Modifier
        .fillMaxWidth()){
        Column(
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Imagen de la habitacion",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)

            )

            Row(

                modifier = Modifier
                    .padding(15.dp)
                    .fillMaxWidth()
                ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(text = room.name,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = room.type,
                        fontSize = 40.sp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(0.dp, 2.dp, 0.dp,2.dp )
                    ) {
                        Text("${room.limit} ",
                            fontSize = 40.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AirlineSeatIndividualSuite,
                            contentDescription = "Limit",
                            modifier = Modifier
                                .size(40.dp)

                        )
                    }

                }
                Column {
                    Text(text = "${room.price} €",
                        fontSize = 40.sp)
                    Text(text = "/noche",
                        fontSize = 25.sp)
                }

            }

            Column(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier
                    .padding(10.dp)

            ){
                Text(text = "Descripción:",
                    fontSize = 30.sp)
                Text(text = room.description,
                    fontSize = 25.sp)

            }

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
            {
                Button(
                    onClick = {  },

                    ) {
                    Text(text = "Reservar")
                }

            }


        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun RoomDetailsPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        RoomDetailsView(room = Room(
            name = "Room 1", type = "Single", number = 101, price = 100.00, occupied = false, limit = 2, offer = 0, description = "This is a room"
        ))
    }
}