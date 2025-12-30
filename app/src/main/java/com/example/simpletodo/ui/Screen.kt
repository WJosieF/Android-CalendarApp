package com.example.simpletodo.ui

sealed class Screen(val route: String) {
    object Todo : Screen("todoFragment")  // 对应menu的todoFragment
    object Calendar : Screen("calendarFragment")  // 对应menu的calendarFragment
    object Note : Screen("noteFragment")  // 对应menu的noteFragment
}