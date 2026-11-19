package org.mtier.timetracker.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.mtier.timetracker.data.api.OcsApi
import javax.inject.Inject
import javax.inject.Singleton

/** Raw hex strings from the capabilities API — parsed to a Compose [Color]
 *  in the UI layer (see ui/theme/Color.kt's parseHexColor), matching how
 *  DTOs elsewhere (e.g. ProjectDto.color) carry colors as strings. */
data class ServerThemeColors(
    val primaryHex: String,
    val onPrimaryHex: String,
)

/**
 * One fetch of `cloud/capabilities` feeds both the server theme colors and
 * the timetracker app's version/feature block. Refreshed after login, on
 * cold start and whenever the app returns to the foreground (MainActivity),
 * so a server update is picked up without signing in again.
 */
@Singleton
class ServerCapabilitiesRepository
    @Inject
    constructor(
        private val api: OcsApi,
    ) {
        private val _colors = MutableStateFlow<ServerThemeColors?>(null)
        val colors: StateFlow<ServerThemeColors?> = _colors.asStateFlow()

        private val _timetracker = MutableStateFlow(TimeTrackerCapabilities.UNKNOWN)
        val timetracker: StateFlow<TimeTrackerCapabilities> = _timetracker.asStateFlow()

        /** Best-effort: on any failure (offline, not logged in yet) the app
         *  keeps the last known values — initially the static Nextcloud-blue
         *  fallback and [TimeTrackerCapabilities.UNKNOWN]. */
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        suspend fun refresh() {
            val sections =
                try {
                    api
                        .getCapabilities()
                        .ocs.data.capabilities
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    return
                }
            sections.theming?.color?.let { primaryHex ->
                _colors.value = ServerThemeColors(primaryHex, sections.theming.colorText ?: "#FFFFFF")
            }
            _timetracker.value =
                sections.timetracker?.let {
                    TimeTrackerCapabilities(
                        published = true,
                        version = it.version,
                        apiVersions = it.api,
                        features = it.features.toSet(),
                    )
                } ?: TimeTrackerCapabilities.UNKNOWN
        }

        /** Called on sign-out, so the next account starts from a clean slate. */
        fun clear() {
            _colors.value = null
            _timetracker.value = TimeTrackerCapabilities.UNKNOWN
        }
    }
