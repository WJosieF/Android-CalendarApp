package com.example.simpletodo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String? = null,              // 可选标题
    val content: String,                    // 笔记内容
    val folderId: Long? = null,             // 所属文件夹ID（可为空，表示未分类）
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isPinned: Boolean = false,          // 是否置顶（第二阶段用）
    val color: Int? = null                  // 颜色标记（第二阶段用）
)