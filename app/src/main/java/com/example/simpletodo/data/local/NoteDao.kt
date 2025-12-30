package com.example.simpletodo.data.local
import androidx.room.*
import com.example.simpletodo.data.model.Folder
import com.example.simpletodo.data.model.Note
import com.example.simpletodo.data.model.NoteWithFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // 获取所有笔记（带文件夹信息）
    @Transaction
    @Query("""
        SELECT * FROM notes 
        ORDER BY 
            CASE WHEN isPinned = 1 THEN 0 ELSE 1 END,
            updatedAt DESC
    """)
    fun getAllNotesWithFolders(): Flow<List<NoteWithFolder>>

    // 获取指定文件夹的笔记（修正版本）
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE 
            (:folderId = 0) OR  -- 0表示"全部"，返回所有笔记
            (:folderId = -1 AND folderId IS NULL) OR  -- -1表示"未分类"
            (folderId = :folderId)  -- 正常文件夹ID
        ORDER BY 
            CASE WHEN isPinned = 1 THEN 0 ELSE 1 END,
            updatedAt DESC
    """)
    fun getNotesByFolder(folderId: Long): Flow<List<NoteWithFolder>>

    // 获取未分类的笔记（通过特殊ID调用）
    fun getUncategorizedNotesWithFolders(): Flow<List<NoteWithFolder>> {
        return getNotesByFolder(Folder.UNCATEGORIZED_FOLDER_ID)
    }

    // 搜索笔记（标题和内容）
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY 
            CASE WHEN isPinned = 1 THEN 0 ELSE 1 END,
            updatedAt DESC
    """)
    fun searchNotesWithFolders(query: String): Flow<List<NoteWithFolder>>

    // 获取单个笔记（带文件夹信息）
    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteByIdWithFolder(noteId: Long): NoteWithFolder?

    @Insert
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    // 批量移动笔记到文件夹
    @Query("UPDATE notes SET folderId = :folderId WHERE id IN (:noteIds)")
    suspend fun moveNotesToFolder(noteIds: List<Long>, folderId: Long?)

    // 获取文件夹中的笔记数量
    @Query("""
        SELECT COUNT(*) FROM notes 
        WHERE 
            (:folderId = 0) OR
            (:folderId = -1 AND folderId IS NULL) OR
            (folderId = :folderId)
    """)
    fun getNoteCountInFolder(folderId: Long): Flow<Int>
}