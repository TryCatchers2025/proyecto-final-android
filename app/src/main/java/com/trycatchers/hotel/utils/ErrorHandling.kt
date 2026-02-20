package com.trycatchers.hotel.utils

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toUserMessage(defaultMessage: String): String {
    println("Error: ${this::class.java.simpleName} - ${message ?: "No message"}")
    return when (this) {
        is IOException ->
            "No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente."

        is HttpException -> this.toHttpErrorMessage(defaultMessage)
        else -> message?.takeIf { it.isNotBlank() } ?: defaultMessage
    }
}

private fun HttpException.toHttpErrorMessage(defaultMessage: String): String {
    val bodyMessage = response()?.errorBody()?.use(ResponseBody::string)?.let(::extractMessage)
    if (!bodyMessage.isNullOrBlank()) {
        return bodyMessage
    }

    val statusMessage = mapStatusCodeToMessage(code())
    if (!statusMessage.isNullOrBlank()) {
        return statusMessage
    }

    return message()?.takeIf { it.isNotBlank() } ?: defaultMessage
}

private fun extractMessage(payload: String): String? {
    if (payload.isBlank()) return null

    return runCatching {
        val json = JSONObject(payload)
        val candidateKeys = listOf("message", "error", "detail", "title")
        candidateKeys
            .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
    }.getOrNull()
}

private fun mapStatusCodeToMessage(code: Int): String? =
    when (code) {
        400, 422 -> "Algunos datos no son válidos. Revisa la información e inténtalo nuevamente."
        401 -> "Tu sesión expiró o no es válida. Inicia sesión para continuar."
        403 -> "No tienes permisos para realizar esta acción."
        404 -> "No encontramos la información solicitada."
        409 -> "Ya existe un registro con los datos proporcionados."
        in 500..504 -> "El servidor tuvo un problema. Inténtalo de nuevo en unos minutos."
        else -> null
    }
