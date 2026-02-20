package com.trycatchers.hotel.data.dtos

/**
 * Representa los filtros que la API de habitaciones espera para devolver disponibilidades. Las
 * claves del mapa generado mantienen el naming camelCase utilizado en el backend.
 */
data class RoomSearchFilters(
    val startDate: String,
    val endDate: String,
    val occupants: Int,
    val onlyOffers: Boolean = false,
    val needsCrib: Boolean = false,
    val needsExtraBed: Boolean = false,
    val isVip: Boolean = false,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
) {

    fun toQueryMap(): Map<String, String> = buildMap {
        put("startDate", startDate)
        put("endDate", endDate)
        put("occupants", occupants.toString())

        if (onlyOffers) put("onlyOffers", "true")
        if (needsCrib) put("cradle", "true")
        if (needsExtraBed) put("extraBed", "true")
        if (isVip) put("vip", "true")

        minPrice?.let { put("pricePerNightMin", it.toString()) }
        maxPrice?.let { put("pricePerNightMax", it.toString()) }
    }
}
