package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.api.OcsApi
import org.mtier.timetracker.data.api.dto.OcsBody
import org.mtier.timetracker.data.api.dto.OcsCapabilitiesDto
import org.mtier.timetracker.data.api.dto.OcsCapabilitiesSectionsDto
import org.mtier.timetracker.data.api.dto.OcsEnvelope
import org.mtier.timetracker.data.api.dto.OcsSelfUserDto
import org.mtier.timetracker.data.api.dto.OcsUsersDetailsDto

class FakeOcsApi : OcsApi {
    var selfUser: OcsSelfUserDto = OcsSelfUserDto(id = "test-user")
    var usersDetails: OcsUsersDetailsDto = OcsUsersDetailsDto()
    var capabilities: OcsCapabilitiesDto = OcsCapabilitiesDto()
    var capabilitiesError: Throwable? = null

    override suspend fun getSelfUser(format: String): OcsEnvelope<OcsSelfUserDto> = OcsEnvelope(OcsBody(selfUser))

    override suspend fun getUsersDetails(format: String): OcsEnvelope<OcsUsersDetailsDto> = OcsEnvelope(OcsBody(usersDetails))

    override suspend fun getCapabilities(format: String): OcsEnvelope<OcsCapabilitiesDto> {
        capabilitiesError?.let { throw it }
        return OcsEnvelope(OcsBody(capabilities))
    }

    companion object {
        fun withTheming(
            color: String?,
            colorText: String?,
        ) = OcsCapabilitiesDto(OcsCapabilitiesSectionsDto(theming = org.mtier.timetracker.data.api.dto.OcsThemingDto(color, colorText)))
    }
}
