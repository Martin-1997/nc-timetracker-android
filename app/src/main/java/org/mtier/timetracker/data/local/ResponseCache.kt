package org.mtier.timetracker.data.local

import org.mtier.timetracker.data.api.dto.ClientDto
import org.mtier.timetracker.data.api.dto.ProjectDto
import org.mtier.timetracker.data.api.dto.TagDto
import javax.inject.Inject
import javax.inject.Singleton

/** [get] returns null on a cache miss (nothing cached yet) — that's a
 *  different case from "server reachable, list legitimately empty", so
 *  repositories can tell "fall back to cache" apart from "cache also has
 *  nothing, rethrow the original network error" (see PLAN.md §4). [clear]
 *  drops both the cached rows and that miss/empty distinction, so a fresh
 *  get() after clear() is a genuine miss again — needed on sign-out so the
 *  next signed-in account doesn't see a previous account's cached list. */
interface ResponseCache<T> {
    suspend fun get(): List<T>?

    suspend fun put(items: List<T>)

    suspend fun clear()
}

interface ProjectsCache : ResponseCache<ProjectDto>

interface ClientsCache : ResponseCache<ClientDto>

interface TagsCache : ResponseCache<TagDto>

/**
 * The actual cache logic, generic over one Room entity/DTO pair — the
 * three [ResponseCache] subtypes above are structurally identical, so
 * rather than triplicate get/put/clear (and the "is this really a miss"
 * bug that comes from getting that logic slightly wrong once, let alone
 * three times) each Room*Cache below just delegates to one instance of
 * this, parameterized with its DAO's methods.
 *
 * put() also skips the table rewrite entirely when the fetched data is
 * identical to what's already cached (Projects/Clients/Tags rarely change
 * between app opens) — the metadata row is still (re)written either way,
 * since "populated" must flip to true even the very first time the fetched
 * list happens to already match an empty table.
 */
@Suppress("LongParameterList")
private class RoomResponseCache<Entity : Any, Dto>(
    private val cacheKey: String,
    private val metadataDao: CacheMetadataDao,
    private val getAllEntities: suspend () -> List<Entity>,
    private val replaceAllEntities: suspend (List<Entity>) -> Unit,
    private val clearEntities: suspend () -> Unit,
    private val toDto: (Entity) -> Dto,
    private val toEntity: (Dto) -> Entity,
) : ResponseCache<Dto> {
    override suspend fun get(): List<Dto>? = if (metadataDao.isPopulated(cacheKey)) getAllEntities().map(toDto) else null

    override suspend fun put(items: List<Dto>) {
        val newEntities = items.map(toEntity)
        if (getAllEntities().toSet() != newEntities.toSet()) {
            replaceAllEntities(newEntities)
        }
        metadataDao.markPopulated(CacheMetadataEntity(cacheKey))
    }

    override suspend fun clear() {
        clearEntities()
        metadataDao.clear(cacheKey)
    }
}

@Singleton
class RoomProjectsCache
    @Inject
    constructor(
        dao: ProjectCacheDao,
        metadataDao: CacheMetadataDao,
    ) : ProjectsCache,
        ResponseCache<ProjectDto> by RoomResponseCache(
            cacheKey = "projects",
            metadataDao = metadataDao,
            getAllEntities = dao::getAll,
            replaceAllEntities = dao::replaceAll,
            clearEntities = dao::clear,
            toDto = { it.toDto() },
            toEntity = { it.toEntity() },
        )

@Singleton
class RoomClientsCache
    @Inject
    constructor(
        dao: ClientCacheDao,
        metadataDao: CacheMetadataDao,
    ) : ClientsCache,
        ResponseCache<ClientDto> by RoomResponseCache(
            cacheKey = "clients",
            metadataDao = metadataDao,
            getAllEntities = dao::getAll,
            replaceAllEntities = dao::replaceAll,
            clearEntities = dao::clear,
            toDto = { it.toDto() },
            toEntity = { it.toEntity() },
        )

@Singleton
class RoomTagsCache
    @Inject
    constructor(
        dao: TagCacheDao,
        metadataDao: CacheMetadataDao,
    ) : TagsCache,
        ResponseCache<TagDto> by RoomResponseCache(
            cacheKey = "tags",
            metadataDao = metadataDao,
            getAllEntities = dao::getAll,
            replaceAllEntities = dao::replaceAll,
            clearEntities = dao::clear,
            toDto = { it.toDto() },
            toEntity = { it.toEntity() },
        )
