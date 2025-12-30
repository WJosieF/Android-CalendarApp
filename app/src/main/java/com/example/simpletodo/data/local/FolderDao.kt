package com.example.simpletodo.data.local

import androidx.room.*
import com.example.simpletodo.data.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    // 获取所有用户创建的文件夹（排除系统文件夹）
    @Query("SELECT * FROM folders WHERE isSystem = 0 ORDER BY name ASC")
    fun getAllUserFolders(): Flow<List<Folder>>

    // 获取所有文件夹（包括系统文件夹）
    @Query("SELECT * FROM folders ORDER BY isSystem ASC, name ASC")
    fun getAllFolders(): Flow<List<Folder>>

    // 检查文件夹名称是否已存在
    @Query("SELECT COUNT(*) FROM folders WHERE name = :name AND isSystem = 0")
    suspend fun folderNameExists(name: String): Int

    // 获取文件夹（按ID）
    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): Folder?

    @Insert
    suspend fun insertFolder(folder: Folder): Long

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    // 获取文件夹内的笔记数量
    @Query("""
        SELECT COUNT(*) FROM notes 
        WHERE 
            (:folderId = 0) 
            OR 
            (:folderId = -1 AND folderId IS NULL)
            OR 
            (folderId = :folderId)
    """)
    fun getNoteCount(folderId: Long): Flow<Int>
}