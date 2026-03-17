package com.example.overdrivexandroid.core.network

import okhttp3.Interceptor
import okhttp3.Response

// AuthInterceptor se encarga de añadir automáticamente el token de autenticación a cada petición HTTP.
// Funciona como un "filtro" en la capa de red: cada vez que la app hace una petición al servidor,
// este interceptor la intercepta y añade la cabecera Authorization con el token Bearer.
//
// Recibe un `tokenProvider` en lugar del token directamente para que siempre lea el valor actual
// de SessionManager (por si el token cambia durante la sesión).
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Si hay token disponible (el usuario está logueado), lo añadimos a la cabecera.
        // Si no hay token (pantalla de login/registro), la petición va sin autenticación.
        tokenProvider()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        // Procedemos con la petición (modificada o no)
        return chain.proceed(requestBuilder.build())
    }
}
