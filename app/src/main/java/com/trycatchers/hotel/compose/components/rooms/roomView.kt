package com.trycatchers.hotel.compose.components.rooms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatIndividualSuite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trycatchers.hotel.R
import com.trycatchers.hotel.compose.screens.RoomCatalogView
import com.trycatchers.hotel.data.api.RoomService
import com.trycatchers.hotel.data.models.Room

@Composable
fun RoomCard(room : Room){
    Box(){
        Row() {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Imagen de la habitacion",
                modifier = Modifier
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .padding(10.dp)

            ){
                Text(text = room.name,
                    fontWeight = FontWeight.Bold
                )
                Text(text = room.type)
                Text(text = "${String.format("%.2f",room.price)} €",
                    fontWeight = FontWeight.Bold,

                )

                Row(
                    modifier = Modifier
                        .padding(0.dp, 2.dp, 0.dp,2.dp )
                ) {
                    Text("${room.limit} ")
                    Icon(
                        imageVector = Icons.Default.AirlineSeatIndividualSuite,
                        contentDescription = "Limit",
                        modifier = Modifier
                            .size(19.dp)


                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RoomCardPreview() {
    RoomCard(room = Room(
        name = "Room 1", type = "Single", number = 101, price = 50.00, occupied = false, limit = 2, offer = 0, description = "This is a room"

    ))
}