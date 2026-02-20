package com.trycatchers.hotel.data.repositories

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor() {
    private var token: String? = null
    private var loggedIn: Boolean = false

    fun saveSession(token: String) {
        this.token = token
        this.loggedIn = true
    }

    fun clearSession() {
        token = null
        loggedIn = false
    }

    fun getToken(): String? = token

    fun isLoggedIn(): Boolean = loggedIn
}

