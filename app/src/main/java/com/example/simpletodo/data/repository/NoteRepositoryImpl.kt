package com.example.simpletodo.data.repository

import com.example.simpletodo.data.local.FolderDao
import com.example.simpletodo.data.local.NoteDao
import com.example.simpletodo.data.model.Folder
import com.example.simpletodo.data.model.Note
import com.example.simpletodo.data.model.NoteWithFolder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val folderDao: FolderDao
) : NoteRepository {

    // 笔记操作
    override fun getAllNotes(): Flow<List<NoteWithFolder>> =
        noteDao.getAllNotesWithFolders()

    override fun getNotesByFolder(folderId: Long): Flow<List<NoteWithFolder>> =
        noteDao.getNotesByFolder(folderId)

    override fun searchNotes(query: String): Flow<List<NoteWithFolder>> =
        noteDao.searchNotesWithFolders(query)

    override fun getUncategorizedNotes(): Flow<List<NoteWithFolder>> =
        noteDao.getUncategorizedNotesWithFolders()

    override suspend fun getNoteById(noteId: Long): NoteWithFolder? =
        noteDao.getNoteByIdWithFolder(noteId)

    override suspend fun addNote(note: Note): Long =
        noteDao.insertNote(note)

    override suspend fun updateNote(note: Note) =
        noteDao.updateNote(note)

    override suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note)

    override suspend fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?) =
        noteDao.moveNotesToFolder(noteIds, folderId)

    // 文件夹操作
    override fun getAllFolders(): Flow<List<Folder>> =
        folderDao.getAllFolders()

    override fun getAllUserFolders(): Flow<List<Folder>> =
        folderDao.getAllUserFolders()

    override suspend fun getFolderById(folderId: Long): Folder? =
        folderDao.getFolderById(folderId)

    override suspend fun addFolder(folder: Folder): Long =
        folderDao.insertFolder(folder)

    override suspend fun updateFolder(folder: Folder) =
        folderDao.updateFolder(folder)

    override suspend fun deleteFolder(folder: Folder) =
        folderDao.deleteFolder(folder)

    override suspend fun folderNameExists(name: String): Boolean =
        folderDao.folderNameExists(name) > 0

    // 统计
    override fun getNoteCountInFolder(folderId: Long): Flow<Int> =
        noteDao.getNoteCountInFolder(folderId)
}