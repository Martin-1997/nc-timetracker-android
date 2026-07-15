package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.EditNameRequest
import org.mtier.timetracker.data.api.dto.TagDto
import org.mtier.timetracker.data.api.throwOnError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
    ) {
        suspend fun getTags(): List<TagDto> = api.getTags().tags

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
