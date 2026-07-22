package org.mtier.timetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CacheMetadataDao {
    @Query("SELECT EXISTS(SELECT 1 FROM cache_metadata WHERE cacheKey = :cacheKey)")
    suspend fun isPopulated(cacheKey: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markPopulated(entity: CacheMetadataEntity)

    @Query("DELETE FROM cache_metadata WHERE cacheKey = :cacheKey")
    suspend fun clear(cacheKey: String)
}

/**
 * Shared shape for the three near-identical cache DAOs below. insertAll()
 * can be fully generic — Room derives the target table from Entity's own
 * @Entity annotation at each concrete call site — and replaceAll()'s
 * clear-then-insert transaction logic only needs an abstract clear() to
 * call. Each concrete DAO still has to provide its own clear() and getAll()
 * (a Room @Query needs a literal SQL string, so the table name can't be
 * made generic), but the rest is written once instead of three times.
 */
abstract class BaseCacheDao<Entity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(entities: List<Entity>)

    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(entities: List<Entity>) {
        clear()
        insertAll(entities)
    }
}

@Dao
abstract class ProjectCacheDao : BaseCacheDao<CachedProjectEntity>() {
    @Query("SELECT * FROM cached_projects")
    abstract suspend fun getAll(): List<CachedProjectEntity>

    @Query("DELETE FROM cached_projects")
    abstract override suspend fun clear()
}

@Dao
abstract class ClientCacheDao : BaseCacheDao<CachedClientEntity>() {
    @Query("SELECT * FROM cached_clients")
    abstract suspend fun getAll(): List<CachedClientEntity>

    @Query("DELETE FROM cached_clients")
    abstract override suspend fun clear()
}

@Dao
abstract class TagCacheDao : BaseCacheDao<CachedTagEntity>() {
    @Query("SELECT * FROM cached_tags")
    abstract suspend fun getAll(): List<CachedTagEntity>

    @Query("DELETE FROM cached_tags")
    abstract override suspend fun clear()
}
