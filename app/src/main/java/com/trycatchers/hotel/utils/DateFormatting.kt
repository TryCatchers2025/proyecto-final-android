package com.trycatchers.hotel.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val legacyDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val localeEs = Locale("es", "ES")
private val apiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val displayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", localeEs)

fun formatDate(millis: Long?): String = millis?.let { legacyDateFormatter.format(Date(it)) } ?: ""

fun formatDisplayDate(date: LocalDate): String = date.format(displayDateFormatter)

fun parseApiDate(raw: String?): LocalDate? =
    raw?.let { runCatching { LocalDate.parse(it, apiDateFormatter) }.getOrNull() }

fun formatApiDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String =
    millisToLocalDate(millis, zoneId).format(apiDateFormatter)

fun millisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

fun formatDateRange(
    startMillis: Long?,
    endMillis: Long?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String? {
    val start = startMillis ?: return null
    val end = endMillis ?: return null
    val startDate = millisToLocalDate(start, zoneId)
    val endDate = millisToLocalDate(end, zoneId)
    return "${formatDisplayDate(startDate)} - ${formatDisplayDate(endDate)}"
}