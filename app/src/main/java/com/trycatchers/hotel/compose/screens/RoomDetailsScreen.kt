package com.trycatchers.hotel.compose.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.trycatchers.hotel.compose.components.booking.BookingErrorCard
import com.trycatchers.hotel.data.models.Review
import com.trycatchers.hotel.data.models.Room
import com.trycatchers.hotel.viewmodels.RoomDetailsViewModel

/** Pantalla de detalle de habitación con galería, reseñas y CTA de reserva. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RoomDetailsScreen(
    onNavigateBack: () -> Unit,
    onBookRoom: (String) -> Unit,
    viewModel: RoomDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            if (state.room != null) {
                BookingBottomBar(room = state.room!!, onBook = { onBookRoom(state.room!!.id) })
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when {
            state.isLoading -> LoadingView(modifier = Modifier.padding(innerPadding))
            state.errorMessage != null ->
                ErrorView(
                    message = state.errorMessage!!,
                    onRetry = viewModel::loadRoomDetails,
                    onBack = onNavigateBack,
                    modifier = Modifier.padding(innerPadding)
                )

            state.room != null ->
                RoomDetailsContent(
                    room = state.room!!,
                    reviews = state.reviews,
                    onBack = onNavigateBack,
                    modifier = Modifier.padding(innerPadding)
                )
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BookingErrorCard(message = message, actionLabel = "Reintentar", onAction = onRetry)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onBack) { Text("Volver") }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoomDetailsContent(
    room: Room,
    reviews: List<Review>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val images = listOfNotNull(room.mainImage) + room.extraImages
    val pagerState = rememberPagerState(pageCount = { images.size.coerceAtLeast(1) })

    LazyColumn(modifier = modifier.fillMaxSize()) {
        // Image Header
        item {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                if (images.isEmpty()) {
                    // Placeholder when no images
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Imagen no disponible",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Gradient overlay
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            Color.Black.copy(
                                                alpha = 0.4f
                                            ),
                                            Color.Transparent,
                                            Color.Transparent
                                        ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                )

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                // Pager Indicators
                if (images.size > 1) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(images.size) { index ->
                            val color =
                                if (pagerState.currentPage == index) Color.White
                                else Color.White.copy(alpha = 0.5f)
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                        }
                    }
                }
            }
        }

        // Room Info
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.name.ifBlank { "Habitación ${room.number}" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (room.rate != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    null,
                                    Modifier.size(16.dp),
                                    tint = Color(0xFFFFC107)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "%.1f".format(room.rate),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }

                Text(
                    text = room.type,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                Text("Características", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                RoomTags(room)

                Spacer(Modifier.height(24.dp))

                Text("Descripción", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = room.description ?: "Sin descripción disponible.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (room.extras.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    Text("Extras incluidos", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        room.extras.forEach { extra ->
                            Text("• $extra", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        // Reviews Section
        if (reviews.isNotEmpty()) {
            item {
                HorizontalDivider(Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                Text(
                    "Reseñas (${reviews.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(20.dp)
                )
            }
            items(reviews) { review -> ReviewItem(review) }
        } else {
            item {
                Spacer(Modifier.height(100.dp)) // Padding for bottom bar
            }
        }
    }
}

@Composable
private fun RoomTags(room: Room) {
    val tags = mutableListOf<String>()
    tags.add("Máx. ${room.occupancyLimit} personas")
    if (room.hasExtraBed) tags.add("Cama supletoria")
    if (room.hasCradle) tags.add("Cuna disponible")

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tags) { tag ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ReviewItem(review: Review) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                Icon(
                    imageVector =
                        if (index < review.rate) Icons.Filled.Star
                        else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = review.createdAt ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = review.comment,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BookingBottomBar(room: Room, onBook: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val price = room.pricePerNight
                val offer = room.offerPercentage

                if (offer != null && offer > 0) {
                    val original = price / (1 - offer / 100)
                    Text(
                        text = "%.2f €".format(original),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                textDecoration =
                                    androidx.compose.ui.text.style.TextDecoration
                                        .LineThrough
                            ),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%.2f €".format(price),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / noche",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onBook,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Reservar ahora") }
        }
    }
}
