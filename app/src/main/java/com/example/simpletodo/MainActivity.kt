package com.example.simpletodo

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.simpletodo.ui.TodoScreen
import com.example.simpletodo.ui.TodoViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest  // 添加这个导入
import androidx.compose.foundation.layout.padding
import com.example.simpletodo.ui.CalendarScreen  // 假设的日历页面
import com.example.simpletodo.ui.NoteScreen     // 假设的笔记页面
import com.example.simpletodo.ui.BottomNav  // 确保有此底部导航组件
import androidx.compose.material3.Scaffold
import com.example.simpletodo.ui.Screen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: TodoViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // 可以显示一个提示,说明为什么需要通知权限
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 检查通知权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
//                PackageManager.PERMISSION_GRANTED
//            ) {
//                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        // 启用 edge-to-edge
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//
//        setContent {
//            MaterialTheme {
//                val systemUiController = rememberSystemUiController()
//                val useDarkIcons = !isSystemInDarkTheme()
//
//                DisposableEffect(systemUiController, useDarkIcons) {
//                    // 更新状态栏和导航栏
//                    systemUiController.setSystemBarsColor(
//                        color = Color.Transparent,
//                        darkIcons = useDarkIcons
//                    )
//                    onDispose {}
//                }
//
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    TodoScreen(viewModel = viewModel)
//                }
//            }
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检查Android 13+的通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // 启用edge-to-edge模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                // 配置状态栏和导航栏样式
                DisposableEffect(systemUiController, useDarkIcons) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                    onDispose {}
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. 创建导航控制器
                    val navController = rememberNavController()

                    // 2. 使用 Scaffold 替代 Column 布局
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            // 3. 将底部导航放在 Scaffold 的 bottomBar 中
                            BottomNav(navController = navController)
                        }
                    ) { paddingValues ->
                        // 4. NavHost 放在 Scaffold 的内容区域
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Todo.route,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)  // 使用 Scaffold 提供的 padding
                        ) {
                            composable(Screen.Todo.route) {
                                TodoScreen(viewModel = viewModel)
                            }
                            composable(Screen.Calendar.route) {
                                CalendarScreen()
                            }
                            composable(Screen.Note.route) {
                                NoteScreen()
                            }
                        }
                    }
                }
            }
        }
    }
} 