package com.trycatchers.hotel.data.models

/**
 * Representa una habitación disponible en el catálogo. Mantiene nombres coherentes con la API de
 * backend para evitar desajustes entre plataformas.
 */
data class Room(
    val id: String = "",
    val name: String = "",
    val number: Int = 0,
    val type: String = "",
    val description: String? = null,
    val mainImage: String? = null,
    val extraImages: List<String> = emptyList(),
    val pricePerNight: Double = 0.0,
    val rate: Double? = null,
    val occupancyLimit: Int = 1,
    val isAvailable: Boolean = true,
    val hasCradle: Boolean = false,
    val hasExtraBed: Boolean = false,
    val offerPercentage: Double? = null,
    val extras: List<String> = emptyList()
)
