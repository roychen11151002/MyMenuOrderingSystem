package com.example.mymenuorderingsystem

import timber.log.Timber

// roy modify for Repository
interface MenuRepository {
    suspend fun getMenuItems(): List<MenuItem>
    suspend fun submitOrder(order: OrderRequest): OrderResponse
}

class MenuRepositoryImpl(private val apiService: MenuApiService) : MenuRepository {
    override suspend fun getMenuItems(): List<MenuItem> {
        return try {
            apiService.fetchMenuItems()
        } catch (e: Exception) {
            Timber.e("網路請求失敗: ${ e.message }")
            emptyList()
        }
    }

    override suspend fun submitOrder(order: OrderRequest): OrderResponse {
        return try {
            apiService.submitOrder(order)
        } catch (e: Exception) {
            Timber.d("訂單提交失敗: ${e.message}")
            OrderResponse(
                status = "error",
                message = "網路連線失敗，請稍後再試"
            )
        }
    }
}

/*
interface MenuRepository {
    fun getMenuItems(): List<MenuItem>
}

class MenuRepositoryImpl : MenuRepository {
    override fun getMenuItems(): List<MenuItem> {
        return when(BuildConfig.API_NAME) {
            "HyperNet" -> listOf(MenuItem(1, "牛肉麵", 150), MenuItem(2, "滷肉飯", 40))
            "iMageTech" -> listOf(MenuItem(1, "紅茶", 30), MenuItem(2, "綠茶", 30),MenuItem(3, "奶茶", 40))
            else -> listOf(MenuItem(1, "香焦", 10), MenuItem(2, "蘋果", 20),MenuItem(3, "木瓜", 80))
        }
    }
}
*/
