package com.example.simpletodo.di

import android.content.Context
import androidx.room.Room
import com.example.simpletodo.data.local.TodoDatabase
import com.example.simpletodo.data.local.TodoDao
import com.example.simpletodo.data.local.TagDao
import com.example.simpletodo.data.local.NoteDao
import com.example.simpletodo.data.local.FolderDao
import com.example.simpletodo.data.repository.TodoRepository
import com.example.simpletodo.data.repository.TodoRepositoryImpl
import com.example.simpletodo.data.repository.TagRepository
import com.example.simpletodo.data.repository.TagRepositoryImpl
import com.example.simpletodo.data.repository.NoteRepository
import com.example.simpletodo.data.repository.NoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.work.WorkManager

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideTodoDatabase(
        @ApplicationContext context: Context
    ): TodoDatabase {
        return Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            "todo_database"
        )
        .addMigrations(
            TodoDatabase.MIGRATION_3_4,
            TodoDatabase.MIGRATION_4_5
        )
        .build()
    }

    
    @Provides
    fun provideTodoDao(database: TodoDatabase): TodoDao {
        return database.todoDao()
    }
    
    @Provides
    fun provideTagDao(database: TodoDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideNoteDao(database: TodoDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideFolderDao(database: TodoDatabase): FolderDao {
        return database.folderDao()
    }
    
    @Provides
    @Singleton
    fun provideTodoRepository(todoDao: TodoDao): TodoRepository {
        return TodoRepositoryImpl(todoDao)
    }
    
    @Provides
    @Singleton
    fun provideTagRepository(tagDao: TagDao): TagRepository {
        return TagRepositoryImpl(tagDao)
    }

    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao, folderDao: FolderDao): NoteRepository {
        return NoteRepositoryImpl(noteDao, folderDao)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
} 