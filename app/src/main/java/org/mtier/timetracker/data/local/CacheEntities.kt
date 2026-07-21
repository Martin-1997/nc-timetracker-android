package org.mtier.timetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.TagDto

/**
 * Thin response cache (PLAN.md §4: "online only... except maybe caching")
 * for Projects/Clients/Tags — the mostly-static reference lists used across
 * every screen's pickers. Deliberately NOT used for anything time-sensitive
 * (work intervals, goals, reports, timelines): those must always reflect
 * the server's current state, which is exactly what this session's
 * cross-device timer sync fix depends on.
 */
@Entity(tableName = "cached_projects")
data class CachedProjectEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val color: String?,
    val clientId: Int?,
    val locked: Int?,
    val archived: Int?,
    val createdAt: Long?,
)

@Entity(tableName = "cached_clients")
data class CachedClientEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val createdAt: Long?,
)

@Entity(tableName = "cached_tags")
data class CachedTagEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val userUid: String?,
    val createdAt: Long?,
)

fun CachedProjectEntity.toDto() = ProjectDto(id, name, color, clientId, locked, archived, createdAt)

fun ProjectDto.toEntity() = CachedProjectEntity(id, name, color, clientId, locked, archived, createdAt)

fun CachedClientEntity.toDto() = ClientDto(id, name, createdAt)

fun ClientDto.toEntity() = CachedClientEntity(id, name, createdAt)

fun CachedTagEntity.toDto() = TagDto(id, name, userUid, createdAt)

fun TagDto.toEntity() = CachedTagEntity(id, name, userUid, createdAt)
