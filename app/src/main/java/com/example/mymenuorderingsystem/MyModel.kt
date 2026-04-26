package com.example.mymenuorderingsystem

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: Int = 0,
    val name: String = "未知餐點",
    val price: Int = 0
)

@Entity(tableName = "order_history")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis(),
    val brandName: String,
    val status: String = "PENDING"
)

@Serializable
data class OrderItemRequest(
    val id: Int,
    val name: String,
    val quantity: Int
)

@Serializable
data class OrderRequest(
    val orderId: String,
    val tableNumber: String,
    val items: List<OrderItemRequest>,
    val note: String,
    val totalAmount: Int
)

@Serializable
data class OrderResponse(
    val status: String,
    val message: String,
    @SerialName("order_timestamp")
    val orderTimestamp: Long? = null
)
