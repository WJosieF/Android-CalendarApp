package com.example.simpletodo.data.repository

import com.example.simpletodo.data.model.Folder
import com.example.simpletodo.data.model.Note
import com.example.simpletodo.data.model.NoteWithFolder
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    // 笔记相关操作
    fun getAllNotes(): Flow<List<NoteWithFolder>>
    fun getNotesByFolder(folderId: Long): Flow<List<NoteWithFolder>>
    fun searchNotes(query: String): Flow<List<NoteWithFolder>>
    fun getUncategorizedNotes(): Flow<List<NoteWithFolder>>
    suspend fun getNoteById(noteId: Long): NoteWithFolder?
    suspend fun addNote(note: Note): Long
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(note: Note)
    suspend fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?)

    // 文件夹相关操作
    fun getAllFolders(): Flow<List<Folder>>
    fun getAllUserFolders(): Flow<List<Folder>>
    suspend fun getFolderById(folderId: Long): Folder?
    suspend fun addFolder(folder: Folder): Long
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(folder: Folder)
    suspend fun folderNameExists(name: String): Boolean

    // 统计
    fun getNoteCountInFolder(folderId: Long): Flow<Int>
}