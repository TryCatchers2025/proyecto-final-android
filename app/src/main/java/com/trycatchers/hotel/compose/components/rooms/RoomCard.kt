package com.trycatchers.hotel.compose.components.rooms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trycatchers.hotel.data.models.Room

@Composable
fun RoomCard(
    room: Room,
    onViewDetails: (String) -> Unit,
    onReserve: (String) -> Unit,
    modifier: Modifier = Modifier,
    actionButton: @Composable ((Modifier) -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RoomImage(room)

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = room.name.ifBlank { "Habitacion ${room.number}" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = room.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = room.description.orEmpty().ifBlank { "Sin descripcion" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${room.pricePerNight.toInt()} EUR / noche",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Capacidad ${room.occupancyLimit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                RoomCardBadges(room)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onViewDetails(room.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ver detalles")
                    }

                    if (actionButton != null) actionButton(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun RoomImage(room: Room) {
    if (!room.mainImage.isNullOrBlank()) {
        AsyncImage(
            model = room.mainImage,
            contentDescription = "Imagen sala ${room.name}",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Sin imagen",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoomCardBadges(room: Room) {
    val badges = buildList {
        if (room.rate != null) {
            add("Valoracion ${"%.1f".format(room.rate)}")
        }
        if ((room.offerPercentage ?: 0.0) > 0.0) {
            add("Oferta ${room.offerPercentage?.toInt()}%")
        }
        if (room.hasCradle) add("Cuna")
        if (room.hasExtraBed) add("Cama extra")
    }

    if (badges.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        badges.forEach { label ->
            AssistChip(
                onClick = {},
                label = { Text(label) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = BorderStroke(0.dp, Color.Transparent),
                modifier = Modifier.wrapContentHeight()
            )
        }
    }
}

@Preview
@Composable
fun RoomCardPreview() {
    val sampleRoom = Room(
        id = "1",
        name = "Suite Deluxe",
        type = "Suite",
        description = "Una habitación espaciosa con vistas al mar, cama king size y baño privado.",
        pricePerNight = 250.0,
        occupancyLimit = 2,
        mainImage = null,
        rate = 4.5,
        offerPercentage = 20.0,
        hasCradle = true,
        hasExtraBed = false
    )

    RoomCard(
        room = sampleRoom,
        onViewDetails = {},
        onReserve = {}
    ) { modifier ->
        OutlinedButton(onClick = {}, modifier = modifier) {
            Text("Reservar")
        }
    }
}