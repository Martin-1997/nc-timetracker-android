package org.mtier.timetracker.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.mtier.timetracker.data.local.AppDatabase
import org.mtier.timetracker.data.local.ClientCacheDao
import org.mtier.timetracker.data.local.ClientsCache
import org.mtier.timetracker.data.local.ProjectCacheDao
import org.mtier.timetracker.data.local.ProjectsCache
import org.mtier.timetracker.data.local.RoomClientsCache
import org.mtier.timetracker.data.local.RoomProjectsCache
import org.mtier.timetracker.data.local.RoomTagsCache
import org.mtier.timetracker.data.local.TagCacheDao
import org.mtier.timetracker.data.local.TagsCache
import javax.inject.Singleton

private const val DATABASE_NAME = "timetracker-cache.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideProjectCacheDao(db: AppDatabase): ProjectCacheDao = db.projectCacheDao()

    @Provides
    fun provideClientCacheDao(db: AppDatabase): ClientCacheDao = db.clientCacheDao()

    @Provides
    fun provideTagCacheDao(db: AppDatabase): TagCacheDao = db.tagCacheDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheBindingsModule {
    @Binds
    abstract fun bindProjectsCache(impl: RoomProjectsCache): ProjectsCache

    @Binds
    abstract fun bindClientsCache(impl: RoomClientsCache): ClientsCache

    @Binds
    abstract fun bindTagsCache(impl: RoomTagsCache): TagsCache
}
