package com.example.overdrivexandroid.core.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.overdrivexandroid.core.session.SessionManager

// RetrofitClient es el punto central de comunicación con el servidor de OverDriveX.
// Usamos el patrón Singleton (object) para que haya una sola instancia de Retrofit en toda la app.
// Se crea la primera vez que se accede a `instance` y se reutiliza siempre.
object RetrofitClient {
    // URL base de la API en producción (Render.com)
    private const val BASE_URL = "https://overdrivexapi.onrender.com/"

    // OkHttpClient con el interceptor de autenticación añadido.
    // El interceptor añade el token Bearer a cada petición automáticamente.
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { SessionManager.currentToken })
        .build()

    // Instancia de Retrofit creada de forma perezosa (lazy): solo se construye cuando se necesita.
    // Usamos Gson para convertir automáticamente el JSON de la API en data classes de Kotlin.
    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
