package org.mtier.timetracker.data.repository

import kotlinx.coroutines.CancellationException
import org.mtier.timetracker.data.local.ResponseCache

/**
 * Network-first with a cache fallback, shared by ProjectsRepository/
 * ClientsRepository/TagsRepository: a successful [fetch] refreshes
 * [cache]; a failed one falls back to the last-cached list, only
 * rethrowing the original error if there's nothing cached either.
 *
 * Deliberately not implemented with `runCatching` — it catches
 * [Throwable], which includes [CancellationException], so a coroutine
 * cancelled mid-fetch (e.g. the screen was navigated away from) would
 * have its cancellation silently swallowed and fall through to a cache
 * read instead of unwinding, breaking structured concurrency. Both catch
 * blocks below rethrow it immediately instead.
 *
 * A [cache].put() failure (e.g. the on-device database is full) is
 * caught separately and best-effort: the fetch itself already succeeded,
 * so that data is still returned even if it couldn't be cached.
 */
@Suppress("TooGenericExceptionCaught", "ThrowsCount")
suspend fun <T> fetchWithCacheFallback(
    cache: ResponseCache<T>,
    fetch: suspend () -> List<T>,
): List<T> {
    val fetched =
        try {
            fetch()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return cache.get() ?: throw e
        }
    try {
        cache.put(fetched)
    } catch (e: CancellationException) {
        throw e
    } catch (
        @Suppress("SwallowedException") e: Exception,
    ) {
        // Best-effort: the network fetch already succeeded, so still
        // return its result even though it couldn't be cached.
    }
    return fetched
}
