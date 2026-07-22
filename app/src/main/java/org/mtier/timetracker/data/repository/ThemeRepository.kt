package org.mtier.timetracker.data.repository

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

@Singleton
class ThemeRepository
    @Inject
    constructor(
        private val api: OcsApi,
    ) {
        private val _colors = MutableStateFlow<ServerThemeColors?>(null)
        val colors: StateFlow<ServerThemeColors?> = _colors.asStateFlow()

        /** Best-effort: on any failure (offline, older server, not logged
         *  in yet) the app just keeps the static Nextcloud-blue fallback. */
        suspend fun refresh() {
            val theming = runCatching { api.getCapabilities().ocs.data.capabilities.theming }.getOrNull()
            val primaryHex = theming?.color ?: return
            _colors.value = ServerThemeColors(primaryHex, theming.colorText ?: "#FFFFFF")
        }
    }
