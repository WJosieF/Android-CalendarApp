package com.example.simpletodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpletodo.data.model.Priority
import com.example.simpletodo.data.model.Tag
import com.example.simpletodo.data.model.Todo
import com.example.simpletodo.data.model.TodoWithTag
import com.example.simpletodo.data.repository.TagRepository
import com.example.simpletodo.data.repository.TodoRepository
import com.example.simpletodo.utils.DateUtils
import com.example.simpletodo.utils.DateUtils.toDatabaseDate
import com.example.simpletodo.utils.DateUtils.toDatabaseFormat
import com.example.simpletodo.utils.DateUtils.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth  // 确保这行存在
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    // 当前选中的日期（默认今天）
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    // 当前显示的月份（默认当前月）
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    // 有任务的日期（用于日历标记）
    private val _markedDates = MutableStateFlow<Set<LocalDate>>(emptySet())
    val markedDates: StateFlow<Set<LocalDate>> = _markedDates

    // 选中日期的任务列表 - 使用新的 getTasksByDate 方法
    val tasksForSelectedDate: StateFlow<List<TodoWithTag>> =
        _selectedDate.flatMapLatest { date ->
            todoRepository.getTasksByDate(date.toDatabaseDate())
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 选中日期的任务统计
    val selectedDateStats: StateFlow<Pair<Int, Int>> =
        tasksForSelectedDate.map { tasks ->
            val total = tasks.size
            val completed = tasks.count { it.todo.isCompleted }
            Pair(total, completed)
        }.stateIn(viewModelScope, SharingStarted.Lazily, Pair(0, 0))

    val tags: StateFlow<List<Tag>> = tagRepository.getAllTags()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // 初始化加载当前月份的任务标记
        loadMarkedDatesForCurrentMonth()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date

        // 如果选择的日期不在当前显示的月份，切换到对应的月份
        val newMonth = YearMonth.from(date)
        if (_currentMonth.value != newMonth) {
            _currentMonth.value = newMonth
            loadMarkedDatesForMonth(newMonth)
        }
    }

    fun goToPreviousMonth() {
        val previousMonth = _currentMonth.value.minusMonths(1)
        _currentMonth.value = previousMonth
        loadMarkedDatesForMonth(previousMonth)

        // 选中新月份的第一天
        _selectedDate.value = previousMonth.atDay(1)
    }

    fun goToNextMonth() {
        val nextMonth = _currentMonth.value.plusMonths(1)
        _currentMonth.value = nextMonth
        loadMarkedDatesForMonth(nextMonth)

        // 选中新月份的第一天
        _selectedDate.value = nextMonth.atDay(1)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.now()
        loadMarkedDatesForCurrentMonth()
    }

    private fun loadMarkedDatesForCurrentMonth() {
        loadMarkedDatesForMonth(_currentMonth.value)
    }

    private fun loadMarkedDatesForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            try {
                // 使用新的 getTaskDatesInMonth 方法
                val dates = todoRepository.getTaskDatesInMonth(
                    yearMonth.toDatabaseFormat()
                ).first()

                _markedDates.value = dates.map { dateString ->
                    dateString.toLocalDate()
                }.toSet()
            } catch (e: Exception) {
                // 处理错误，可以记录日志
                e.printStackTrace()
                _markedDates.value = emptySet()
            }
        }
    }

    // 添加任务
    fun addTodo(
        title: String,
        note: String?,
        tagId: Long?,
        priority: Priority,
        dueDate: LocalDateTime?, // 从日历传入
        enableReminder: Boolean
    ) {
        viewModelScope.launch {
            try {
                val todo = Todo(
                    title = title,
                    note = note,
                    tagId = tagId,
                    priority = priority,
                    dueDate = dueDate,
                    enableReminder = enableReminder
                )
                todoRepository.addTodo(todo)

                // 如果添加的任务在当前显示的月份，更新标记
                dueDate?.toLocalDate()?.let { date ->
                    if (YearMonth.from(date) == _currentMonth.value) {
                        loadMarkedDatesForCurrentMonth()
                    }

                    // 如果添加的任务在选中的日期，任务列表会自动更新（Flow）
                    if (date == _selectedDate.value) {
                        // Flow 会自动更新，不需要手动刷新
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleTodo(todo: TodoWithTag) {
        viewModelScope.launch {
            try {
                todoRepository.updateTodo(todo.todo.copy(isCompleted = !todo.todo.isCompleted))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteTodo(todo: TodoWithTag) {
        viewModelScope.launch {
            try {
                val taskDate = todo.todo.dueDate?.toLocalDate()
                todoRepository.deleteTodo(todo.todo)

                // 如果删除的任务在当前显示的月份，更新标记
                if (taskDate?.let {
                        YearMonth.from(it) == _currentMonth.value
                    } == true) {
                    loadMarkedDatesForCurrentMonth()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateTodo(
        todo: TodoWithTag,
        title: String,
        note: String?,
        tagId: Long?,
        priority: Priority,
        dueDate: LocalDateTime?,
        enableReminder: Boolean
    ) {
        viewModelScope.launch {
            try {
                val oldDate = todo.todo.dueDate?.toLocalDate()
                val updatedTodo = todo.todo.copy(
                    title = title,
                    note = note,
                    tagId = tagId,
                    priority = priority,
                    dueDate = dueDate,
                    enableReminder = enableReminder
                )
                todoRepository.updateTodo(updatedTodo)

                // 检查日期是否改变，如果改变则更新标记
                val newDate = dueDate?.toLocalDate()
                if (oldDate != newDate) {
                    loadMarkedDatesForCurrentMonth()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}