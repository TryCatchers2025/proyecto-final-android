package com.trycatchers.hotel.data.network

import com.trycatchers.hotel.data.repositories.SessionRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor encargado de añadir el token de sesión a cada petición realizada por Retrofit.
 * Respeta peticiones sin token (sesiones aún no inicializadas) sin mutarlas.
 */
class AuthorizationInterceptor
@Inject
constructor(
    private val sessionRepository: SessionRepository,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionRepository.getToken()

        val requestWithAuth =
            if (!token.isNullOrBlank()) {
                originalRequest.newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                originalRequest
            }

        return chain.proceed(requestWithAuth)
    }
}
