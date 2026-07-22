package org.mtier.timetracker.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Every Nextcloud OCS endpoint wraps its payload in this envelope. */
@Serializable
data class OcsEnvelope<T>(
    val ocs: OcsBody<T>,
)

@Serializable
data class OcsBody<T>(
    val data: T,
)

/** `ocs/v1.php/cloud/user` (self) — used to detect admin-group membership. */
@Serializable
data class OcsSelfUserDto(
    val id: String,
    val groups: List<String> = emptyList(),
)

/** A single entry under `ocs/v2.php/cloud/users/details`'s `data.users` map. */
@Serializable
data class OcsUserDetailDto(
    val id: String,
    val displayname: String? = null,
)

@Serializable
data class OcsUsersDetailsDto(
    val users: Map<String, OcsUserDetailDto> = emptyMap(),
)

/** `ocs/v1.php/cloud/capabilities`'s `data.capabilities.theming` section. */
@Serializable
data class OcsCapabilitiesDto(
    val capabilities: OcsCapabilitiesSectionsDto = OcsCapabilitiesSectionsDto(),
)

@Serializable
data class OcsCapabilitiesSectionsDto(
    val theming: OcsThemingDto? = null,
)

@Serializable
data class OcsThemingDto(
    val color: String? = null,
    @SerialName("color-text") val colorText: String? = null,
)
