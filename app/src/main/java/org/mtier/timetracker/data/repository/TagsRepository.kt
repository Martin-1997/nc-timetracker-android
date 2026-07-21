package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.EditNameRequest
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.api.throwOnError
import org.mtier.timetracker.data.local.TagsCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
        private val cache: TagsCache,
    ) {
        /** Network-first with a cache fallback on failure — see
         *  fetchWithCacheFallback()'s kdoc for the rationale. */
        suspend fun getTags(): List<TagDto> = fetchWithCacheFallback(cache) { api.getTags().tags }

        suspend fun addTag(name: String) {
            api.addTag(name).throwOnError()
        }

        suspend fun editTag(
            id: Int,
            name: String,
        ) {
            api.editTag(id, EditNameRequest(name)).throwOnError()
        }

        suspend fun deleteTag(id: Int) {
            api.deleteTag(id).throwOnError()
        }
    }
