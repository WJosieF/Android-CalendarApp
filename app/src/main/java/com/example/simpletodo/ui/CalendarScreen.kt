@file:Suppress("EXPERIMENTAL_MATERIAL3_API")

package com.example.simpletodo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simpletodo.data.model.Priority
import com.example.simpletodo.data.model.TodoWithTag
import com.example.simpletodo.utils.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val tasksForSelectedDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val selectedDateStats by viewModel.selectedDateStats.collectAsStateWithLifecycle()
    val markedDates by viewModel.markedDates.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历") },
                actions = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(Icons.Default.Today, "今天")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "添加任务")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 月份切换栏
            MonthHeader(
                currentMonth = currentMonth,
                onPreviousMonth = { viewModel.goToPreviousMonth() },
                onNextMonth = { viewModel.goToNextMonth() },
                onTitleClick = { showMonthYearPicker = true }
            )

            // 星期标题行
            WeekDayHeader()

            // 月历网格
            MonthGrid(
                currentMonth = currentMonth,
                selectedDate = selectedDate,
                markedDates = markedDates,
                onDateSelected = { date -> viewModel.selectDate(date) }
            )

            // 分隔线
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 当日任务列表
            DayTasksSection(
                selectedDate = selectedDate,
                tasks = tasksForSelectedDate,
                stats = selectedDateStats,
                tags = tags,
                onToggleTodo = viewModel::toggleTodo,
                onDeleteTodo = viewModel::deleteTodo,
                onUpdateTodo = viewModel::updateTodo
            )
        }

        // 添加任务对话框
        if (showAddDialog) {
            CalendarAddTodoDialog(
                tags = tags,
                initialDueDate = selectedDate.atTime(23, 59), // 默认当天23:59
                onDismiss = { showAddDialog = false },
                onConfirm = { title, note, tagId, priority, dueDate, enableReminder ->
                    viewModel.addTodo(title, note, tagId, priority, dueDate, enableReminder)
                    showAddDialog = false
                }
            )
        }

        // 新增：月份年份选择器对话框
        if (showMonthYearPicker) {
            MonthYearPickerDialog(
                currentMonth = currentMonth,
                onDismiss = { showMonthYearPicker = false },
                onDateSelected = { yearMonth ->
                    // 切换到选中的年月，选中该月第一天
                    viewModel.selectDate(yearMonth.atDay(1))
                    showMonthYearPicker = false
                }
            )
        }
    }
}

@Composable
fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTitleClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.ArrowBackIosNew,
                contentDescription = "上个月",
                modifier = Modifier.size(20.dp)
            )
        }

        // 使月份标题可点击
        Box(
            modifier = Modifier
                .clickable { onTitleClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = DateUtils.getMonthDisplayName(currentMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary // 改为蓝色表示可点击
            )
        }

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = "下个月",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun WeekDayHeader() {
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        weekDays.forEach { day ->
            Box(
                modifier = Modifier
                    .weight(1f)  // 正确的写法
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MonthGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    markedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = DateUtils.getDaysInMonth(currentMonth)
    val firstDayOfWeek = DateUtils.getFirstDayOfWeek(currentMonth)

    // 计算需要填充的空白单元格
    val emptyCells = if (firstDayOfWeek == 7) 0 else firstDayOfWeek - 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // 总共6行，每行7天
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 6) {
                    val index = row * 7 + col

                    if (index < emptyCells) {
                        // 空白单元格
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                        )
                    } else {
                        val dayIndex = index - emptyCells
                        if (dayIndex < daysInMonth.size) {
                            val date = daysInMonth[dayIndex]
                            Box(
                                modifier = Modifier
                                    .weight(1f)  // 这里使用 weight
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            ) {
                                CalendarDay(
                                    date = date,
                                    isSelected = date == selectedDate,
                                    hasTasks = markedDates.contains(date),
                                    onClick = { onDateSelected(date) }
                                )
                            }
                        } else {
                            // 超出当月天数的空白单元格
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 新增：月份年份选择器对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onDateSelected: (YearMonth) -> Unit
) {
    val currentYear = LocalDate.now().year
    val years = (currentYear - 5..currentYear + 5).toList() // 前后5年
    val months = (1..12).toList()

    var selectedYear by remember { mutableStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableStateOf(currentMonth.monthValue) }
    var expandedYear by remember { mutableStateOf(false) }
    var expandedMonth by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期") },
        text = {
            Column {
                // 年份选择
                Text(
                    text = "年份",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear }
                ) {
                    TextField(
                        value = "$selectedYear 年",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text("$year 年") },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 月份选择
                Text(
                    text = "月份",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth }
                ) {
                    TextField(
                        value = "$selectedMonth 月",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text("$month 月") },
                                onClick = {
                                    selectedMonth = month
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedYearMonth = YearMonth.of(selectedYear, selectedMonth)
                    onDateSelected(selectedYearMonth)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit
) {
    val isToday = DateUtils.isToday(date)

    Box(
        modifier = Modifier
            .fillMaxSize()  // 改为 fillMaxSize
            .clip(CircleShape)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 16.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                textAlign = TextAlign.Center
            )

            if (hasTasks) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@Composable
fun DayTasksSection(
    selectedDate: LocalDate,
    tasks: List<TodoWithTag>,
    stats: Pair<Int, Int>,
    tags: List<com.example.simpletodo.data.model.Tag>,
    onToggleTodo: (TodoWithTag) -> Unit,
    onDeleteTodo: (TodoWithTag) -> Unit,
    onUpdateTodo: (TodoWithTag, String, String?, Long?, Priority, LocalDateTime?, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // 日期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtils.getDateDisplayName(selectedDate),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${stats.second}/${stats.first}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "这一天没有待办事项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "点击右上角+按钮添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            // 任务列表
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(tasks) { todoWithTag ->
                    CalendarTodoItem(
                        todoWithTag = todoWithTag,
                        tags = tags,
                        onToggle = { onToggleTodo(todoWithTag) },
                        onDelete = { onDeleteTodo(todoWithTag) },
                        onUpdateTodo = onUpdateTodo
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarTodoItem(
    todoWithTag: TodoWithTag,
    tags: List<com.example.simpletodo.data.model.Tag>,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onUpdateTodo: (TodoWithTag, String, String?, Long?, Priority, LocalDateTime?, Boolean) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showEditDialog = true },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(
                    checked = todoWithTag.todo.isCompleted,
                    onCheckedChange = { onToggle() }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = todoWithTag.todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        color = if (todoWithTag.todo.isCompleted) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // 显示时间
                    todoWithTag.todo.dueDate?.let { dueDate ->
                        Text(
                            text = dueDate.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // 优先级指示器
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(todoWithTag.todo.priority.color))
            )
        }
    }

    // 编辑对话框（复用现有的 EditTodoDialog）
    if (showEditDialog) {
        // 这里需要导入 EditTodoDialog
        // 如果 EditTodoDialog 在同一个包中，可以直接调用
        // 否则需要导入: import com.example.simpletodo.ui.EditTodoDialog

        // 你可以从 TodoScreen.kt 复制 EditTodoDialog 函数到这里
        // 或者创建一个共享的对话框组件

        // 暂时注释掉，你需要导入正确的 EditTodoDialog
         EditTodoDialog(
             todoWithTag = todoWithTag,
             tags = tags,
             onDismiss = { showEditDialog = false },
             onConfirm = { title, note, tagId, priority, dueDate, enableReminder ->
                 onUpdateTodo(
                     todoWithTag,
                     title,
                     note,
                     tagId,
                     priority,
                     dueDate,
                     enableReminder
                 )
                 showEditDialog = false
             },
             onDelete = {
                 onDelete()
                 showEditDialog = false
             }
         )
    }
}

// 专门为日历页面的添加对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarAddTodoDialog(
    tags: List<com.example.simpletodo.data.model.Tag>,
    initialDueDate: LocalDateTime? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Long?, Priority, LocalDateTime?, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf<Long?>(null) }
    var selectedPriority by remember { mutableStateOf(Priority.LOW) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var enableReminder by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加任务") },
        text = {
            Column {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("请输入任务内容") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 备注输入框
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("添加备注(可选)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 日期显示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("截止日期")
                    Text(
                        text = if (dueDate != null) {
                            dueDate!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        } else {
                            "未设置（将使用日历选中日期）"
                        },
                        color = if (dueDate == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 优先级选择
                Text("优先级", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = {
                                Text(
                                    when (priority) {
                                        Priority.LOW -> "低"
                                        Priority.MEDIUM -> "中"
                                        Priority.HIGH -> "高"
                                    }
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(priority.color))
                                )
                            }
                        )
                    }
                }

                // 标签选择
                Text("选择标签", style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTagId == null,
                        onClick = { selectedTagId = null },
                        label = { Text("无标签") }
                    )
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTagId == tag.id,
                            onClick = { selectedTagId = tag.id },
                            label = { Text(tag.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(tag.color))
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // 使用传入的 dueDate（来自日历选中日期）
                    onConfirm(
                        text,
                        note.ifBlank { null },
                        selectedTagId,
                        selectedPriority,
                        dueDate, // 使用预填的日期
                        enableReminder
                    )
                },
                enabled = text.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}