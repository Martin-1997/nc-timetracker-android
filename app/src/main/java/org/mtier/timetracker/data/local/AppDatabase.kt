package org.mtier.timetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedProjectEntity::class, CachedClientEntity::class, CachedTagEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectCacheDao(): ProjectCacheDao

    abstract fun clientCacheDao(): ClientCacheDao

    abstract fun tagCacheDao(): TagCacheDao
}
