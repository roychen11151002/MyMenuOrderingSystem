package com.example.mymenuorderingsystem

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mymenuorderingsystem.ui.theme.MyMenuOrderingSystemTheme
import timber.log.Timber
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

/*
// roy modify for timber log
        if(BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
        Timber.tag("RoyChen").d("Main onCreate: ${ BuildConfig.FLAVOR }, ${ BuildConfig.API_URL },${ BuildConfig.API_MODE }")
        Timber.d("Main onCreate: ${ Build.MANUFACTURER }, ${ BuildConfig.API_SHOW }, ${ BuildConfig.API_NAME }")
*/

        setContent {
            MyMenuOrderingSystemTheme {
/*
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
*/
                MainScreen()
            }
        }
    }
}

class LineNumberDebugTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val emoji = when(priority) {
            Log.VERBOSE -> "\uD83D\uDFE3"   // purple color
            Log.DEBUG -> "\uD83D\uDD35"     // blue color
            Log.INFO -> "\uD83D\uDFE2"          // green color
            Log.WARN -> "\uD83D\uDFE0"          // orange color
            Log.ERROR -> "\uD83D\uDD34"     // red color
            else -> "⚪"                    // white color
        }
        val stackTrace = Throwable().stackTrace
        val element = stackTrace.find { it.className !in listOf(
            Timber::class.java.name,
            Timber.Tree::class.java.name,
            Timber.DebugTree::class.java.name,
            LineNumberDebugTree::class.java.name,
            "timber.log.Timber\$Forest"
        )}
        val suffix = element?.let { " (${it.fileName}:${ it.lineNumber })" } ?: ""

        super.log(priority, tag, emoji + message + suffix, t)
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter ) {
            OrderSystemApp()
        }
/*
        Column(modifier = Modifier.padding(16.dp)) {
            Text("This content")
            Text("Hello World!!")
            Text("Hello Roy Chen!!")
        }
*/
    }
}

@Composable
fun MainScreen() {
    Scaffold(
        topBar = {MyTopBar()},
        bottomBar = {MyBottomBar()},
        floatingActionButton = {MyFAB()}
    ) { innerPadding -> MainContent(modifier = Modifier.padding(innerPadding).fillMaxSize())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name))},
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
fun MyBottomBar() {
    NavigationBar{
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = null)},
            label = {Text("Home")}
        )
    }
}

@Composable
fun MyFAB() {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    FloatingActionButton(onClick = {},
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Preview(showBackground = true, name = "light color")
@Composable
fun MainScreenPreview() {
    MyMenuOrderingSystemTheme {
        MainScreen()
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true, name = "dark color")
@Composable
fun MainScreenDarkPreview() {
    MyMenuOrderingSystemTheme {
        MainScreen()
    }
}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MaterialTheme {
        Greeting("Android")
    }
}
*/
