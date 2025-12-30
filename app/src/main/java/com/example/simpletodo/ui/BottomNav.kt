package com.example.simpletodo.ui

import com.example.simpletodo.R

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNav(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // 获取当前导航页面的路由
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // 底部导航项列表（与menu.xml对应）
        val items = listOf(
            Screen.Todo,
            Screen.Calendar,
            Screen.Note
        )

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    // 根据路由加载对应图标（使用你定义的ic_todo等）
                    Icon(
                        painter = painterResource(
                            id = when (screen) {
                                is Screen.Todo -> R.drawable.ic_todo
                                is Screen.Calendar -> R.drawable.ic_calendar
                                is Screen.Note -> R.drawable.ic_note
                            }
                        ),
                        contentDescription = screen.route
                    )
                },
                label = {
                    // 显示标题（与menu.xml一致）
                    Text(
                        text = when (screen) {
                            is Screen.Todo -> "待办"
                            is Screen.Calendar -> "日历"
                            is Screen.Note -> "笔记"
                        }
                    )
                },
                // 判断当前是否选中（路由匹配）
                selected = currentRoute == screen.route,
                // 点击切换页面
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            // 清除之前的页面栈，避免返回键多次跳转
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // 避免重复创建相同页面实例
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}