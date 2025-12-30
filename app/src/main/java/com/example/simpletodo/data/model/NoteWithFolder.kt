package com.example.simpletodo.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithFolder(
    @Embedded
    val note: Note,

    @Relation(
        parentColumn = "folderId",
        entityColumn = "id"
    )
    val folder: Folder?
)