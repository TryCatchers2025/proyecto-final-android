package com.trycatchers.hotel.compose.components.rooms

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trycatchers.hotel.R
import com.trycatchers.hotel.compose.screens.RoomCatalogView
import com.trycatchers.hotel.data.api.RoomService
import com.trycatchers.hotel.data.models.Room

@Composable
fun RoomCard(room : Room, onClick: (Room) -> Unit){
    Box(
        modifier = Modifier
        .fillMaxWidth()
            .clickable{onClick(room)}
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = "Imagen de la habitacion",
                modifier = Modifier
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                        .fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .padding(15.dp)
                        .weight(1f)

                ){
                    Text(text = room.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(text = room.type,
                        fontSize = 20.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(0.dp, 2.dp, 0.dp,2.dp )
                    ) {
                        Text("${room.limit} ",
                            fontSize = 20.sp
                        )
                        Icon(
                            imageVector = Icons.Default.AirlineSeatIndividualSuite,
                            contentDescription = "Limit",
                            modifier = Modifier
                                .size(20.dp)

                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .padding(10.dp)


                ) {
                    Text(text = "${String.format("%.2f",room.price)} €",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(text = "/noche")
                }


            }

        }
    }

}

@Preview(showBackground = true)
@Composable
fun RoomCardPreview() {
    RoomCard(room = Room( roomId = "123",
        name = "Room 1", type = "Single", number = 101, price = 50.00, occupied = false, limit = 2, offer = 0, description = "This is a room"

    )){}
}