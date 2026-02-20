package com.trycatchers.hotel.utils

import com.trycatchers.hotel.BuildConfig

/**
 * Configuración centralizada de la API
 * Los valores se definen en build.gradle.kts (buildConfigField)
 * - Debug: localhost:3000
 * - Release: servidor de producción
 */
object ApiConfig {
    val BASE_URL = BuildConfig.API_BASE_URL
}
