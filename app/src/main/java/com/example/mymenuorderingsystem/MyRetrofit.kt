package com.example.mymenuorderingsystem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class OrderItemRequest(
    val id: Int,
    val name: String,
    val quantity: Int
)

@Serializable
data class OrderResponse(
    val status: String,
    val message: String,
    @SerialName("order_timestamp")
    val orderTimestamp: Long? = null
)

@Serializable
data class OrderRequest(
    val orderId: String,
    val tableNumber: String,
    val items: List<OrderItemRequest>,
    val note: String,
    val totalAmount: Int
)

interface MenuApiService {
    @GET("menu.json")
    suspend fun fetchMenuItems(): List<MenuItem>

    @POST("orders/submit")
    suspend fun submitOrder(@Body order: OrderRequest): OrderResponse
}

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    single {
        Retrofit.Builder()
            // .baseUrl("https://github.com")
            .baseUrl("https://raw.githubusercontent.com/roychen11151002/MyMenuOrderingSystem/master/")
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MenuApiService::class.java)
    }
}