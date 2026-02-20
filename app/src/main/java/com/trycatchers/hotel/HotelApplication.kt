package com.trycatchers.hotel

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application raíz del cliente Android.
 * Habilita la inicialización global de dependencias mediante Hilt.
 */
@HiltAndroidApp
class HotelApplication : Application()
