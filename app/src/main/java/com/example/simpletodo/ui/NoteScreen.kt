package com.example.simpletodo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.simpletodo.data.model.Folder
import com.example.simpletodo.data.model.NoteWithFolder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteScreen(
    viewModel: NoteViewModel = hiltViewModel()
) {
    var showSearch by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedNotes by remember { mutableStateOf(setOf<Long>()) }
    var showMultiDeleteConfirmDialog by remember { mutableStateOf(false) }

    // 新增状态：编辑笔记和删除笔记
    var noteToEdit by remember { mutableStateOf<NoteWithFolder?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteWithFolder?>(null) }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val allFolders by viewModel.allFolders.collectAsStateWithLifecycle()
    val selectedFolder by viewModel.selectedFolder.collectAsStateWithLifecycle()
    val folderNoteCounts by viewModel.folderNoteCounts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isMultiSelectMode) {
                        Text("已选择 ${selectedNotes.size} 项")
                    } else {
                        Text("笔记")
                    }
                },
                navigationIcon = {
                    if (isMultiSelectMode) {
                        IconButton(
                            onClick = {
                                isMultiSelectMode = false
                                selectedNotes = emptySet()
                            }
                        ) {
                            Icon(Icons.Default.Close, "退出多选")
                        }
                    }
                },
                actions = {
                    if (isMultiSelectMode) {
                        IconButton(
                            onClick = { showMultiDeleteConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.Delete, "批量删除")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                showSearch = !showSearch
                                if (!showSearch) viewModel.updateSearchQuery("")
                            }
                        ) {
                            Icon(
                                imageVector = if (showSearch) {
                                    Icons.Default.Close
                                } else {
                                    Icons.Default.Search
                                },
                                contentDescription = if (showSearch) "关闭搜索" else "搜索"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isMultiSelectMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加笔记")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框（动画显示）
            AnimatedVisibility(
                visible = showSearch,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        placeholder = { Text("搜索笔记...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        "清空搜索",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // 文件夹列表
            FolderList(
                folders = allFolders,
                selectedFolderId = selectedFolder?.id ?: Folder.ALL_FOLDER_ID,
                folderNoteCounts = folderNoteCounts,
                onFolderSelected = viewModel::selectFolder,
                onAddFolderClick = { showAddFolderDialog = true },
                onDeleteFolder = viewModel::deleteFolder
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 笔记列表
            Box(modifier = Modifier.weight(1f)) {
                if (notes.isEmpty()) {
                    EmptyNoteState(
                        modifier = Modifier.fillMaxSize(),
                        onAddClick = { showAddDialog = true }
                    )
                } else {
                    NoteList(
                        notes = notes,
                        isMultiSelectMode = isMultiSelectMode,
                        selectedNoteIds = selectedNotes,
                        onNoteClick = { note ->
                            if (isMultiSelectMode) {
                                selectedNotes = if (selectedNotes.contains(note.note.id)) {
                                    selectedNotes - note.note.id
                                } else {
                                    selectedNotes + note.note.id
                                }
                            } else {
                                // 点击笔记进入编辑模式
                                noteToEdit = note
                            }
                        },
                        onNoteLongClick = { note ->
                            if (!isMultiSelectMode) {
                                isMultiSelectMode = true
                                selectedNotes = setOf(note.note.id)
                            }
                        },
                        onEditClick = { note ->
                            noteToEdit = note
                        },
                        onDeleteClick = { note ->
                            noteToDelete = note
                        }
                    )
                }
            }
        }
    }

    // 添加笔记对话框
    if (showAddDialog) {
        AddNoteDialog(
            currentFolderId = selectedFolder?.id,
            userFolders = allFolders.filter { it.id > 0 }, // 排除虚拟文件夹
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, folderId ->
                viewModel.addNote(title, content, folderId)
                showAddDialog = false
            }
        )
    }

    // 编辑笔记对话框
    if (noteToEdit != null) {
        EditNoteDialog(
            noteWithFolder = noteToEdit!!,
            userFolders = allFolders.filter { it.id > 0 },
            onDismiss = { noteToEdit = null },
            onConfirm = { title, content, folderId ->
                viewModel.updateNote(noteToEdit!!, title, content, folderId)
                noteToEdit = null
            },
            onDelete = {
                viewModel.deleteNote(noteToEdit!!)
                noteToEdit = null
            }
        )
    }

    // 单个删除确认对话框
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("确认删除") },
            text = {
                val title = noteToDelete?.note?.title
                Text("确定要删除笔记\"${title ?: "无标题笔记"}\"吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(noteToDelete!!)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 添加文件夹对话框
    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name ->
                viewModel.addFolder(name)
                showAddFolderDialog = false
            }
        )
    }

    // 批量删除确认对话框
    if (showMultiDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${selectedNotes.size} 个笔记吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: 批量删除逻辑（暂时不实现）
                        isMultiSelectMode = false
                        selectedNotes = emptySet()
                        showMultiDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMultiDeleteConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun FolderList(
    folders: List<Folder>,
    selectedFolderId: Long,
    folderNoteCounts: Map<Long, Int>,
    onFolderSelected: (Long) -> Unit,
    onAddFolderClick: () -> Unit,
    onDeleteFolder: (Folder) -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf<Folder?>(null) }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(folders) { folder ->
            val noteCount = folderNoteCounts[folder.id] ?: 0

            var showMenu by remember { mutableStateOf(false) }

            Surface(
                modifier = Modifier
                    .clickable { onFolderSelected(folder.id) }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                if (!folder.isSystem) {
                                    showDeleteConfirmDialog = folder
                                }
                            }
                        )
                    },
                shape = MaterialTheme.shapes.small,
                color = if (selectedFolderId == folder.id) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    1.dp,
                    if (selectedFolderId == folder.id) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (folder.isSystem) {
                        Icon(
                            imageVector = when (folder.id) {
                                Folder.ALL_FOLDER_ID -> Icons.Default.Folder
                                Folder.UNCATEGORIZED_FOLDER_ID -> Icons.Default.FolderOpen
                                else -> Icons.Default.Folder
                            },
                            contentDescription = folder.name,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Text("${folder.name} ($noteCount)")

                    // 如果是用户文件夹，显示更多选项菜单
                    if (!folder.isSystem) {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "更多选项",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("删除文件夹") },
                                    onClick = {
                                        showMenu = false
                                        showDeleteConfirmDialog = folder
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "删除"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedIconButton(
                onClick = onAddFolderClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "添加文件夹",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // 删除文件夹确认对话框
    showDeleteConfirmDialog?.let { folder ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("删除文件夹") },
            text = {
                val noteCount = folderNoteCounts[folder.id] ?: 0
                if (noteCount > 0) {
                    Text("文件夹 ${folder.name} 中有 $noteCount 个笔记，删除后这些笔记将移到'未分类'。")
                } else {
                    Text("确定要删除文件夹 ${folder.name} 吗？")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteFolder(folder)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun NoteList(
    notes: List<NoteWithFolder>,
    isMultiSelectMode: Boolean,
    selectedNoteIds: Set<Long>,
    onNoteClick: (NoteWithFolder) -> Unit,
    onNoteLongClick: (NoteWithFolder) -> Unit,
    onEditClick: (NoteWithFolder) -> Unit,
    onDeleteClick: (NoteWithFolder) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes) { noteWithFolder ->
            NoteItem(
                noteWithFolder = noteWithFolder,
                isMultiSelectMode = isMultiSelectMode,
                isSelected = selectedNoteIds.contains(noteWithFolder.note.id),
                onClick = { onNoteClick(noteWithFolder) },
                onLongClick = { onNoteLongClick(noteWithFolder) },
                onEditClick = { onEditClick(noteWithFolder) },
                onDeleteClick = { onDeleteClick(noteWithFolder) }
            )
        }
    }
}

@Composable
fun NoteItem(
    noteWithFolder: NoteWithFolder,
    isMultiSelectMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 第一行：标题和更多按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 标题（可为空）
                Column(modifier = Modifier.weight(1f)) {
                    if (!noteWithFolder.note.title.isNullOrBlank()) {
                        Text(
                            text = noteWithFolder.note.title!!,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "无标题笔记",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 更多选项按钮
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "更多选项",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "编辑")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 内容预览
            Text(
                text = noteWithFolder.note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 底部信息栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 文件夹标签
                if (noteWithFolder.folder != null) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = noteWithFolder.folder.name,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "未分类",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 时间
                Text(
                    text = formatNoteTime(noteWithFolder.note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// 编辑笔记对话框
@Composable
fun EditNoteDialog(
    noteWithFolder: NoteWithFolder,
    userFolders: List<Folder>,
    onDismiss: () -> Unit,
    onConfirm: (String?, String, Long?) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(noteWithFolder.note.title ?: "") }
    var content by remember { mutableStateOf(noteWithFolder.note.content) }
    var selectedFolderId by remember { mutableStateOf(noteWithFolder.note.folderId) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑笔记") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("标题（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("内容") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 文件夹选择
                Text(
                    text = "选择文件夹",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFolderId == null,
                        onClick = { selectedFolderId = null },
                        label = { Text("未分类") }
                    )
                    userFolders.forEach { folder ->
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = { selectedFolderId = folder.id },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧删除按钮
                TextButton(
                    onClick = { showDeleteConfirmDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }

                // 右侧取消/确定按钮
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = {
                            onConfirm(
                                title.ifBlank { null },
                                content,
                                selectedFolderId
                            )
                        },
                        enabled = content.isNotBlank()
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    )

    // 删除确认对话框
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("确认删除") },
            text = {
                Text("确定要删除这个笔记吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun EmptyNoteState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = "空笔记",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "你还没有笔记",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮添加吧！",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddClick) {
            Text("创建第一个笔记")
        }
    }
}

// 添加笔记对话框（保持不变）
@Composable
fun AddNoteDialog(
    currentFolderId: Long?,
    userFolders: List<Folder>,
    onDismiss: () -> Unit,
    onConfirm: (String?, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedFolderId by remember { mutableStateOf<Long?>(currentFolderId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新笔记") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("标题（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("内容") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 文件夹选择
                Text(
                    text = "选择文件夹",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFolderId == null,
                        onClick = { selectedFolderId = null },
                        label = { Text("未分类") }
                    )
                    userFolders.forEach { folder ->
                        FilterChip(
                            selected = selectedFolderId == folder.id,
                            onClick = { selectedFolderId = folder.id },
                            label = { Text(folder.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(title.ifBlank { null }, content, selectedFolderId)
                    }
                },
                enabled = content.isNotBlank()
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

// 添加文件夹对话框（保持不变）
@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加新文件夹") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("文件夹名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                },
                enabled = name.isNotBlank()
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

// 辅助函数：格式化时间（保持不变）
private fun formatNoteTime(dateTime: java.time.LocalDateTime): String {
    val now = java.time.LocalDateTime.now()
    val days = java.time.temporal.ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate())

    return when {
        days == 0L -> "今天 ${dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
        days == 1L -> "昨天 ${dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}"
        days < 7 -> "${days}天前"
        else -> dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}