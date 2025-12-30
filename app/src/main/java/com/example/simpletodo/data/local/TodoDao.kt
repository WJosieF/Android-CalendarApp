package com.example.simpletodo.data.local

import androidx.room.*
import com.example.simpletodo.data.model.Todo
import com.example.simpletodo.data.model.TodoWithTag
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Transaction
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodosWithTags(): Flow<List<TodoWithTag>>

    @Transaction
    @Query("""
        SELECT * FROM todos 
        WHERE isCompleted = 0 
        ORDER BY 
            CASE 
                WHEN dueDate IS NULL THEN 1 
                ELSE 0 
            END,
            dueDate ASC,
            createdAt DESC
    """)
    fun getUncompletedTodosWithTags(): Flow<List<TodoWithTag>>

    @Transaction
    @Query("""
        SELECT * FROM todos 
        WHERE isCompleted = 1 
        ORDER BY 
            CASE 
                WHEN dueDate IS NULL THEN 1 
                ELSE 0 
            END,
            dueDate ASC,
            createdAt DESC
    """)
    fun getCompletedTodosWithTags(): Flow<List<TodoWithTag>>

    @Transaction
    @Query("SELECT * FROM todos WHERE tagId = :tagId ORDER BY createdAt DESC")
    fun getTodosByTagWithTags(tagId: Long): Flow<List<TodoWithTag>>

    @Insert
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Transaction
    @Query("""
    SELECT * FROM todos 
    WHERE strftime('%Y-%m', dueDate) = :yearMonth 
    AND dueDate IS NOT NULL
    ORDER BY dueDate ASC
""")
    fun getTasksByMonth(yearMonth: String): Flow<List<TodoWithTag>>

    @Query("""
    SELECT DISTINCT date(dueDate) as taskDate 
    FROM todos 
    WHERE dueDate IS NOT NULL 
    AND strftime('%Y-%m', dueDate) = :yearMonth
""")
    fun getTaskDatesInMonth(yearMonth: String): Flow<List<String>>

    @Transaction
    @Query("""
    SELECT * FROM todos 
    WHERE date(dueDate) = :date 
    ORDER BY 
        CASE 
            WHEN time(dueDate) IS NULL THEN 1 
            ELSE 0 
        END,
        dueDate ASC,
        priority DESC
""")
    fun getTasksByDate(date: String): Flow<List<TodoWithTag>>

    @Query("""
    SELECT COUNT(*) 
    FROM todos 
    WHERE date(dueDate) = :date 
    AND isCompleted = 0
""")
    fun getUncompletedTaskCountByDate(date: String): Flow<Int>

    @Query("""
    SELECT COUNT(*) 
    FROM todos 
    WHERE date(dueDate) = :date
""")
    fun getTotalTaskCountByDate(date: String): Flow<Int>
} 