package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val id: Int,
    val name: String,
    val userUid: String? = null,
    val createdAt: Long? = null,
)

@Serializable
data class TagsResponse(
    @SerialName("Tags") val tags: List<TagDto> = emptyList(),
)

@Serializable
data class ClientDto(
    val id: Int,
    val name: String,
    val createdAt: Long? = null,
)

@Serializable
data class ClientsResponse(
    @SerialName("Clients") val clients: List<ClientDto> = emptyList(),
)
