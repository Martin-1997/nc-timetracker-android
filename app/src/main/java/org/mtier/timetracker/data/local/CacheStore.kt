package org.mtier.timetracker.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wipes every disposable response-cache table in one call. AppDatabase
 * holds nothing but cache data (PLAN.md §4), so a blanket wipe is always
 * safe here — unlike enumerating each Room*Cache's clear() by hand at the
 * call site, a new cache added later is covered automatically instead of
 * silently leaking a previous account's data on sign-out if that call gets
 * missed.
 */
interface CacheStore {
    suspend fun clearAll()
}

@Singleton
class RoomCacheStore
    @Inject
    constructor(
        private val db: AppDatabase,
    ) : CacheStore {
        // clearAllTables() is a blocking call and must not run on the main thread.
        override suspend fun clearAll() = withContext(Dispatchers.IO) { db.clearAllTables() }
    }
