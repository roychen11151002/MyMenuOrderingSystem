package com.example.mymenuorderingsystem

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MenuApiService {
    @GET("menu.json")
    suspend fun fetchMenuItems(): List<MenuItem>

    // @POST("https://webhook.site/1c31346d-ffe1-451d-9ab1-09fa2df6f952")
    @POST("https://webhook.site/123072a0-3854-43b1-b156-89bc6ddb5896")
    suspend fun submitOrder(
        @Body order: OrderRequest,
        @Header("x-api-key") apiKey: String = "000000"
    ): OrderResponse
}

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single<MenuApiService> {
        Retrofit.Builder()
            // .baseUrl("https://github.com")
            .baseUrl(BuildConfig.API_MENU_URL)
            //github.com/roychen11151002/MyMenuOrderingSystem.git
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MenuApiService::class.java)
    }
}
