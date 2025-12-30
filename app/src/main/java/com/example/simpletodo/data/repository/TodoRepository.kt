package com.example.simpletodo.data.repository

import com.example.simpletodo.data.model.Todo
import com.example.simpletodo.data.model.TodoWithTag
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getAllTodos(): Flow<List<TodoWithTag>>
    fun getUncompletedTodos(): Flow<List<TodoWithTag>>
    fun getCompletedTodos(): Flow<List<TodoWithTag>>
    fun getTodosByTag(tagId: Long): Flow<List<TodoWithTag>>
    suspend fun addTodo(todo: Todo): Long
    suspend fun updateTodo(todo: Todo)
    suspend fun deleteTodo(todo: Todo)

    // 新增日历相关方法
    fun getTasksByMonth(yearMonth: String): Flow<List<TodoWithTag>>
    fun getTaskDatesInMonth(yearMonth: String): Flow<List<String>>
    fun getTasksByDate(date: String): Flow<List<TodoWithTag>>
    fun getUncompletedTaskCountByDate(date: String): Flow<Int>
    fun getTotalTaskCountByDate(date: String): Flow<Int>
} 