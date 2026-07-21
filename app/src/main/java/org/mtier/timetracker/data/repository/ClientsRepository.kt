package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.EditNameRequest
import org.mtier.timetracker.data.api.throwOnError
import org.mtier.timetracker.data.local.ClientsCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
        private val cache: ClientsCache,
    ) {
        /** Network-first with a cache fallback on failure — see
         *  ProjectsRepository.getProjects()'s kdoc for the rationale. */
        suspend fun getClients(): List<ClientDto> =
            runCatching { api.getClients().clients }
                .onSuccess { cache.put(it) }
                .getOrElse { networkError -> cache.get() ?: throw networkError }

        suspend fun addClient(name: String) {
            api.addClient(name).throwOnError()
        }

        suspend fun editClient(
            id: Int,
            name: String,
        ) {
            api.editClient(id, EditNameRequest(name)).throwOnError()
        }

        suspend fun deleteClient(id: Int) {
            api.deleteClient(id).throwOnError()
        }
    }
