package com.example.mymenuorderingsystem

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import timber.log.Timber

@Serializable
data class MenuItem(
    val id: Int = 0,
    val name: String = "未知餐點",
    val price: Int = 0
)

class MainViewModel(private val repository: MenuRepository) : ViewModel() {
    val customName = BuildConfig.API_NAME
    val url = BuildConfig.API_URL
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
    var selectedTable by mutableStateOf("")
    var note by mutableStateOf("")
    var isProcessing by mutableStateOf(false)
        private set

    private fun generateOrderId(): String {
        val timestamp = System.currentTimeMillis()
        val randomSuffix = (100..999).random()

        return "ORD-$timestamp-$randomSuffix"
    }
    fun placeOrder(onSuccess: () -> Unit) {
        orderId = generateOrderId()
        viewModelScope.launch {
            isProcessing = true
            delay(2000)
            isProcessing = false
            onSuccess()
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
            .build()

        WorkManager.getInstance(context).enqueue(uploadRequest)
        android.util.Log.d("RoyChen", "訂單已排程, ID: $orderId")
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