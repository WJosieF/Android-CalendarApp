package com.example.simpletodo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.simpletodo.data.model.Tag
import com.example.simpletodo.data.model.Todo
import com.example.simpletodo.data.model.Note
import com.example.simpletodo.data.model.Folder

@Database(
    entities = [
        Todo::class,
        Tag::class,
        Note::class,
        Folder::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun tagDao(): TagDao

    abstract fun noteDao(): NoteDao

    abstract fun folderDao(): FolderDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE todos ADD COLUMN dueDate TEXT"
                )
                database.execSQL(
                    "ALTER TABLE todos ADD COLUMN enableReminder INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE todos ADD COLUMN note TEXT"
                )
            }
        }

    }
} 