package com.example.simpletodo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                       // 文件夹名称
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isSystem: Boolean = false           // 是否为系统文件夹（全部、未分类）
) {
    companion object {
        const val ALL_FOLDER_ID = 0L        // "全部" - 虚拟文件夹
        const val UNCATEGORIZED_FOLDER_ID = -1L // "未分类" - 特殊ID

    }

    // 检查是否是虚拟文件夹
    val isVirtualFolder: Boolean
        get() = id == ALL_FOLDER_ID || id == UNCATEGORIZED_FOLDER_ID

}
