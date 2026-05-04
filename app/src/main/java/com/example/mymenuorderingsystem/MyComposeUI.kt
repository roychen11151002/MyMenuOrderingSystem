package com.example.mymenuorderingsystem

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

enum class Screen(val route: String) {
    MENU("menu_page"),
    CHECKOUT("checkout_page"),
    HISTORY("history_page"),
    HELP("help_page")
}

@Composable
fun OrderSystemApp(modifier: Modifier) {
    val mainVm: MainViewModel = koinViewModel()
    val navController = rememberNavController()
    val toNavigate: (Screen) -> Unit = { screen -> navController.navigate(screen.route) }
    val toBack: () -> Unit = { navController.popBackStack(Screen.MENU.route, inclusive = false) }

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Screen.MENU.route
        ) {
            composable(Screen.MENU.route) {
                MenuScreen(
                    mainVm = mainVm,
                    onNavigate = toNavigate         // reference
                )
            }

            composable(Screen.CHECKOUT.route) {
                CheckoutScreen(
                    mainVm = mainVm,
                    onNavigate = toNavigate,         // reference
                    onDone = toBack
                    )
            }

            composable(Screen.HISTORY.route) {
                HistoryScreen(
                    mainVm = mainVm,
                    onNavigate = toNavigate,         // reference
                    onBack = toBack
                )
            }

            composable(Screen.HELP.route) {
                HelpScreen(
                    mainVm = mainVm,
                    onBack = toBack
                )
            }
        }
    }
}

@Composable
fun CommonTopBar(onHelpClick: () ->Unit) {
    Row(modifier = Modifier.padding(0.dp)) {

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onHelpClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Help, contentDescription = "Help")
        }
/*
        Button(onClick = onHelpClick, modifier = Modifier
            .height(6.dp)
            .width(6.dp)) {
            Text("Help")
        }
*/
    }
}

@Composable
fun MenuScreen(
    mainVm: MainViewModel = koinViewModel(),
    onNavigate : (Screen) -> Unit
) {
    Column(Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        CommonTopBar(onHelpClick = { onNavigate(Screen.HELP) })

        Text("歡迎來到 ${mainVm.customName}", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(mainVm.menuItems, key = { it.name }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.name} $${item.price}")
                    Button(onClick = { mainVm.addToCart(item) }) { Text("加入") }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { onNavigate(Screen.HISTORY) }, Modifier.fillMaxWidth()) {
            Text("歷史訂單紀錄")
        }

        Spacer(Modifier.height(20.dp))

        Button(onClick = { onNavigate(Screen.CHECKOUT) }, Modifier.fillMaxWidth()) {
            Text("去結帳 (${mainVm.cart.size} 項目)")
        }
    }
}

@Composable
fun CheckoutScreen(
    mainVm: MainViewModel = koinViewModel(),
    onNavigate: (Screen) -> Unit,
    orderVm: OrderViewModel = koinViewModel(),
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> }

    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        CommonTopBar(onHelpClick = { onNavigate(Screen.HELP) })

        Text("結帳清單", style = MaterialTheme.typography.headlineMedium)
        // mainVm.cart.forEach { Text("- ${it.name}") }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(mainVm.cart) { item ->
                Text("- ${ item.name } ($${ item.price })", modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Text("外送費: $${ mainVm.mode }")

        TextField(
            value = orderVm.note,
            onValueChange = { orderVm.note = it },
            label = { Text("訂單備註") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                orderVm.placeOrder(context, mainVm.cart) {
                    mainVm.clearCart()
                    onDone()
                }
            },
            enabled = !orderVm.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if(orderVm.isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("確定支付")
            }
        }
/*
        Button(
            onClick = {
                scope.launch {
                    isProcessing = true
                    delay(2000) // 模擬網路傳輸 (Coroutine)
                    isProcessing = false
                    onDone()
                }
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isProcessing) "下單中..." else "確認支付")
        }
*/
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    mainVm: MainViewModel = koinViewModel(),
    onNavigate: (Screen) -> Unit,
    historyVm: HistoryViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val historyList by historyVm.historyList.collectAsState()

    Column(Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        CommonTopBar(onHelpClick = { onNavigate(Screen.HELP) })

        Text("歷史訂單紀錄", style = MaterialTheme.typography.headlineMedium)

        if(historyList.isEmpty()) {
            Text("尚無訂單紀錄", color = Color.Gray, modifier = Modifier.weight(1f))
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(historyList, key = { it.orderId }) { order ->
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.currentValue) {
                        if(dismissState.currentValue == SwipeToDismissBoxValue.EndToStart)
                            historyVm.deleteOrder(order)
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val backgroundColor  = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> Color.Red
                                else -> Color.Transparent
                            }
                            Column (
                                Modifier.fillMaxSize()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(backgroundColor)
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Black)
                            }
                        },
                        content = {
                            Card(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(8.dp)) {
                                    Text("訂單號: ${order.orderId}", style = MaterialTheme.typography.bodySmall)
                                    Text("品牌: ${order.brandName}", color = MaterialTheme.colorScheme.primary)
                                    Text("備註: ${order.note.ifEmpty { "無備註" } }")
                                }
                            }
                        }
                    )
                }
            }
        }

        Button(onClick = onBack, Modifier.fillMaxWidth()) { Text("返回首頁") }
    }
}

@Composable
fun HelpScreen(
    mainVm: MainViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    Column(Modifier
        .fillMaxSize()
        .padding(8.dp)) {
        Text(text = "${ mainVm.customName }幫助中心", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        Text(text ="customer URL: ${BuildConfig.API_URL }")

        Text(text ="ordering menu URL: ${BuildConfig.API_MENU_URL }")

        Text(text ="mode: ${ mainVm.mode }", modifier = Modifier.weight(1f))

        Button( onClick = { onBack() }, modifier = Modifier.fillMaxWidth() ) { Text("返回") }
    }
}

