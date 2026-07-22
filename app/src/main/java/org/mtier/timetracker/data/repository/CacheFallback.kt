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
 *
 * A [cache].get() failure while falling back (e.g. a Room I/O error) is
 * also caught: it's treated as a cache miss so the *original* network
 * error is what gets rethrown, not the fallback's own failure — matching
 * this function's documented contract of only ever surfacing the network
 * error.
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
            val cached =
                try {
                    cache.get()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (
                    @Suppress("SwallowedException") cacheError: Exception,
                ) {
                    null
                }
            return cached ?: throw e
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
