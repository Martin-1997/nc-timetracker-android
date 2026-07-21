package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.local.CacheStore

class FakeCacheStore : CacheStore {
    var cleared = false
        private set

    var clearError: Throwable? = null

    override suspend fun clearAll() {
        clearError?.let { throw it }
        cleared = true
    }
}
