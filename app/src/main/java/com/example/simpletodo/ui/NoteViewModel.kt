package com.example.simpletodo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpletodo.data.model.Folder
import com.example.simpletodo.data.model.Note
import com.example.simpletodo.data.model.NoteWithFolder
import com.example.simpletodo.data.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    // 当前选中的文件夹ID
    private val _selectedFolderId = MutableStateFlow(Folder.ALL_FOLDER_ID)
    val selectedFolderId: StateFlow<Long> = _selectedFolderId

    // 搜索查询
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 所有笔记（根据选中的文件夹和搜索查询过滤）
    val notes: StateFlow<List<NoteWithFolder>> = combine(
        noteRepository.getAllNotes(),
        _selectedFolderId,
        _searchQuery
    ) { allNotes, folderId, query ->
        val filteredByFolder = if (folderId == Folder.ALL_FOLDER_ID) {
            allNotes  // 全部文件夹：显示所有笔记
        } else if (folderId == Folder.UNCATEGORIZED_FOLDER_ID) {
            allNotes.filter { it.note.folderId == null }  // 未分类：folderId为null
        } else {
            allNotes.filter { it.note.folderId == folderId }  // 具体文件夹
        }

        // 应用搜索过滤
        if (query.isNotBlank()) {
            filteredByFolder.filter { noteWithFolder ->
                noteWithFolder.note.title?.contains(query, ignoreCase = true) == true ||
                        noteWithFolder.note.content.contains(query, ignoreCase = true)
            }
        } else {
            filteredByFolder
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 用户文件夹列表（从数据库获取）
    val userFolders: StateFlow<List<Folder>> = noteRepository.getAllUserFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 完整的文件夹列表（包括虚拟文件夹）
    val allFolders: StateFlow<List<Folder>> = combine(
        userFolders,
        selectedFolderId
    ) { userFolders, selectedId ->
        // 构建完整列表：虚拟文件夹 + 用户文件夹
        val virtualFolders = listOf(
            Folder(id = Folder.ALL_FOLDER_ID, name = "全部", isSystem = true),
            Folder(id = Folder.UNCATEGORIZED_FOLDER_ID, name = "未分类", isSystem = true)
        )
        virtualFolders + userFolders
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(
            Folder(id = Folder.ALL_FOLDER_ID, name = "全部", isSystem = true),
            Folder(id = Folder.UNCATEGORIZED_FOLDER_ID, name = "未分类", isSystem = true)
        )
    )

    // 当前选中的文件夹对象
    val selectedFolder: StateFlow<Folder?> = combine(
        allFolders,
        selectedFolderId
    ) { folders, folderId ->
        folders.find { it.id == folderId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // 每个文件夹的笔记数量
    val folderNoteCounts: StateFlow<Map<Long, Int>> = combine(
        noteRepository.getAllNotes(),
        allFolders
    ) { allNotes, folders ->
        folders.associate { folder ->
            val count = when (folder.id) {
                Folder.ALL_FOLDER_ID -> allNotes.size
                Folder.UNCATEGORIZED_FOLDER_ID -> allNotes.count { it.note.folderId == null }
                else -> allNotes.count { it.note.folderId == folder.id }
            }
            folder.id to count
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // 操作函数
    fun selectFolder(folderId: Long) {
        _selectedFolderId.value = folderId
        println("DEBUG: 选择了文件夹 ID: $folderId") // 临时调试
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addNote(title: String?, content: String, folderId: Long?) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                folderId = folderId,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            noteRepository.addNote(note)
        }
    }

    fun updateNote(noteWithFolder: NoteWithFolder, title: String?, content: String, folderId: Long?) {
        viewModelScope.launch {
            val updatedNote = noteWithFolder.note.copy(
                title = title,
                content = content,
                folderId = folderId,
                updatedAt = LocalDateTime.now()
            )
            noteRepository.updateNote(updatedNote)
        }
    }

    fun deleteNote(noteWithFolder: NoteWithFolder) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteWithFolder.note)
        }
    }

    fun addFolder(name: String) {
        viewModelScope.launch {
            // 检查名称是否已存在
            if (!noteRepository.folderNameExists(name)) {
                val folder = Folder(
                    name = name,
                    createdAt = LocalDateTime.now(),
                    isSystem = false
                )
                noteRepository.addFolder(folder)
            }
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            // 先检查文件夹是否为空
            val noteCount = folderNoteCounts.value[folder.id] ?: 0
            if (noteCount == 0) {
                noteRepository.deleteFolder(folder)
            } else {
                // 非空文件夹：将笔记移到未分类，然后删除文件夹
                // 1. 获取该文件夹下的所有笔记
                val notesInFolder = notes.value.filter { it.note.folderId == folder.id }

                // 2. 将这些笔记的folderId设为null（移到未分类）
                notesInFolder.forEach { noteWithFolder ->
                    val updatedNote = noteWithFolder.note.copy(
                        folderId = null,
                        updatedAt = LocalDateTime.now()
                    )
                    noteRepository.updateNote(updatedNote)
                }

                // 3. 删除文件夹
                noteRepository.deleteFolder(folder)
            }
        }
    }

    fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?) {
        viewModelScope.launch {
            noteRepository.moveNotesToFolder(noteIds, folderId)
        }
    }
}