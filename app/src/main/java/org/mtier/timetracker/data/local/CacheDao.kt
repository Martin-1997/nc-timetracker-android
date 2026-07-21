package org.mtier.timetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ProjectCacheDao {
    @Query("SELECT * FROM cached_projects")
    suspend fun getAll(): List<CachedProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedProjectEntity>)

    @Query("DELETE FROM cached_projects")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(entities: List<CachedProjectEntity>) {
        clear()
        insertAll(entities)
    }
}

@Dao
interface ClientCacheDao {
    @Query("SELECT * FROM cached_clients")
    suspend fun getAll(): List<CachedClientEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedClientEntity>)

    @Query("DELETE FROM cached_clients")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(entities: List<CachedClientEntity>) {
        clear()
        insertAll(entities)
    }
}

@Dao
interface TagCacheDao {
    @Query("SELECT * FROM cached_tags")
    suspend fun getAll(): List<CachedTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CachedTagEntity>)

    @Query("DELETE FROM cached_tags")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(entities: List<CachedTagEntity>) {
        clear()
        insertAll(entities)
    }
}
