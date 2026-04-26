package com.example.mymenuorderingsystem

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainViewModel(private val repository: MenuRepository) : ViewModel() {
    val customName = BuildConfig.API_NAME
    val mode = BuildConfig.API_MODE

    private val _cart = mutableStateListOf<MenuItem>()
    val cart: List<MenuItem> = _cart

    // val menuItems: List<MenuItem> = repository.getMenuItems()
    private val _menuItems = mutableStateListOf<MenuItem>()
    val menuItems: List<MenuItem> = _menuItems

    init {
        fetchMenuData()
    }

    private fun fetchMenuData() {
        viewModelScope.launch {
            val result = repository.getMenuItems()
            _menuItems.clear()
            _menuItems.addAll(result)
        }
    }

    fun addToCart(item: MenuItem) {
        _cart.add(item)
    }

    fun clearCart() {
        _cart.clear()
    }
}

class OrderViewModel(
    private val orderDao: OrderDao,
    private val repository: MenuRepository
) : ViewModel() {
    private var orderId: String = ""
    var selectedTable by mutableStateOf("1")
    var note by mutableStateOf("")
    var isProcessing by mutableStateOf(false)
        private set

    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val randomSuffix = (100..999).random()

        return "ORD-$timestamp-$randomSuffix"
    }

    fun placeOrder(
        context: Context,
        cartItems: List<MenuItem>,
        onSuccess: () -> Unit) {
        if (isProcessing) return

        isProcessing = true
        orderId = generateOrderId()
        val itemsRequest = cartItems.map { item ->
            OrderItemRequest(
                id = item.id,
                name = item.name,
                quantity = 1
            )
        }

        val totalAmount = cartItems.sumOf { it.price }

        val orderRequest = OrderRequest(
            orderId = orderId,
            tableNumber = selectedTable,
            note = note,
            totalAmount = totalAmount,
            items = itemsRequest
        )

        viewModelScope.launch {
            try {
                // delay(1000)

                val response = repository.submitOrder(orderRequest)

                if (response.status == "success") {
                // if(true) {
                    Timber.d("訂單提交成功: ${response.message}")
                    saveOrderToHistory("MyBrand")
                    startOrderUpload(context, note)
                    onSuccess()
                } else {
                    Timber.e("伺服器回傳失敗: ${response.message}")
                }
            } catch (e: Exception) {
                Timber.e("提交過程發生錯誤: ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }

    fun startOrderUpload(context: Context, orderNote: String) {
        val inputData = workDataOf(
            "ORDER_ID" to orderId,
            "ORDER_NOTE" to orderNote
            )

        val uploadRequest = OneTimeWorkRequestBuilder<OrderUploadWorker>()
            .setInputData(inputData)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(uploadRequest)
        Timber.d("訂單已排程, ID: $orderId")
    }

    fun saveOrderToHistory(brand: String) {
        viewModelScope.launch {
            val entity = OrderEntity(
                orderId = orderId,
                note = note,
                brandName = brand
            )
            orderDao.insertOrder(entity)
            Timber.d("RoyChen: 訂單已存入 Room: $orderId")
        }
    }
/*
    fun submitOrderToServer(order: OrderRequest) {
        viewModelScope.launch {
            try {
                isProcessing = true
                val response = repository.submitOrder(order)
                if (response.status == "success") {
                    Timber.d("訂單提交成功: ${response.message}")
                }
            } catch (e: Exception) {
                Timber.e("提交失敗: ${ e.message }")
            } finally {
                isProcessing = false
            }
        }
    }
*/
}

class HistoryViewModel(private val orderDao: OrderDao) : ViewModel() {
    val historyList: StateFlow<List<OrderEntity>> = orderDao.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            orderDao.deleteOrder(order)
        }
    }
}