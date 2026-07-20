package org.mtier.timetracker.data.api

import org.mtier.timetracker.data.api.dto.OcsCapabilitiesDto
import org.mtier.timetracker.data.api.dto.OcsEnvelope
import org.mtier.timetracker.data.api.dto.OcsSelfUserDto
import org.mtier.timetracker.data.api.dto.OcsUsersDetailsDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Nextcloud's core OCS API (as opposed to the timetracker app's own
 * AjaxController endpoints in [TimeTrackerApi]) — used for admin-group
 * detection, the allowed-users picker, and server-theming colors.
 * `format=json` is required or Nextcloud returns XML.
 */
interface OcsApi {
    @Headers("Accept: application/json")
    @GET("ocs/v1.php/cloud/user")
    suspend fun getSelfUser(
        @Query("format") format: String = "json",
    ): OcsEnvelope<OcsSelfUserDto>

    @Headers("Accept: application/json")
    @GET("ocs/v2.php/cloud/users/details")
    suspend fun getUsersDetails(
        @Query("format") format: String = "json",
    ): OcsEnvelope<OcsUsersDetailsDto>

    @Headers("Accept: application/json")
    @GET("ocs/v1.php/cloud/capabilities")
    suspend fun getCapabilities(
        @Query("format") format: String = "json",
    ): OcsEnvelope<OcsCapabilitiesDto>
}
