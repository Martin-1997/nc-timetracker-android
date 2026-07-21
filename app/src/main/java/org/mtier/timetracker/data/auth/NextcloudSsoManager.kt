package org.mtier.timetracker.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.exceptions.AccountImportCancelledException
import com.nextcloud.android.sso.exceptions.AndroidGetAccountsPermissionNotGranted
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppNotInstalledException
import com.nextcloud.android.sso.helper.SingleAccountHelper
import com.nextcloud.android.sso.model.SingleSignOnAccount
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SsoEvent {
    data class AccountPicked(
        val credentials: Credentials,
    ) : SsoEvent

    data object Cancelled : SsoEvent

    data class Error(
        val message: String,
    ) : SsoEvent
}

/**
 * Wraps the Android-SingleSignOn library (PLAN.md §3/§9 — a v1.x
 * convenience layered onto the standalone Login Flow v2, not a v1.0
 * requirement): if the Nextcloud Files app is installed, its own account +
 * network stack can be reused instead of a fresh browser login. The
 * library's account-picker result only reaches the app via
 * Activity.onActivityResult, so MainActivity forwards into here and this
 * class republishes the outcome as a [SharedFlow] any ViewModel can collect
 * — that's the only reason this exists as a singleton rather than living
 * directly in LoginViewModel, which doesn't have an Activity reference of
 * its own.
 */
@Singleton
class NextcloudSsoManager
    @Inject
    constructor() {
        private val _events = MutableSharedFlow<SsoEvent>(extraBufferCapacity = 1)
        val events: SharedFlow<SsoEvent> = _events.asSharedFlow()

        fun isFilesAppInstalled(context: Context): Boolean =
            isFilesAppInstalled { pkg -> runCatching { context.packageManager.getPackageInfo(pkg, 0) }.isSuccess }

        /** Launches the library's account chooser. Its result arrives later,
         *  via [handleActivityResult] — see this class's kdoc. */
        fun pickAccount(activity: Activity) {
            try {
                AccountImporter.pickNewAccount(activity)
            } catch (e: NextcloudFilesAppNotInstalledException) {
                _events.tryEmit(SsoEvent.Error(e.message ?: "Nextcloud app not installed"))
            } catch (e: AndroidGetAccountsPermissionNotGranted) {
                // The library itself triggers the runtime permission request
                // here; handlePermissionsResult() below receives the user's
                // answer, but pickAccount() must be called again afterward
                // to actually open the chooser — surface that as a
                // retry-driving message rather than a hard failure.
                _events.tryEmit(SsoEvent.Error(e.message ?: "Grant account access, then try again"))
            }
        }

        fun handleActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?,
            activity: Activity,
        ) {
            try {
                AccountImporter.onActivityResult(
                    requestCode,
                    resultCode,
                    data,
                    activity,
                    object : AccountImporter.IAccountAccessGranted {
                        override fun accountAccessGranted(account: SingleSignOnAccount) {
                            SingleAccountHelper.commitCurrentAccount(activity, account.name)
                            val credentials = Credentials(serverUrl = account.url, username = account.userId, appPassword = account.token)
                            _events.tryEmit(SsoEvent.AccountPicked(credentials))
                        }
                    },
                )
            } catch (
                @Suppress("SwallowedException") e: AccountImportCancelledException,
            ) {
                // The user backing out of the account chooser is a normal,
                // expected outcome here, not a failure worth logging.
                _events.tryEmit(SsoEvent.Cancelled)
            }
        }

        fun handlePermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray,
            activity: Activity,
        ) {
            AccountImporter.onRequestPermissionsResult(requestCode, permissions, grantResults, activity)
        }
    }

/** Same three packages the library's own AndroidManifest.xml <queries>
 *  block declares visibility for (prod/QA/beta). A plain top-level function
 *  taking the "is this package installed" check as a parameter, rather than
 *  a NextcloudSsoManager method, so the "is any known package installed"
 *  logic is unit-testable without a real PackageManager. */
internal fun isFilesAppInstalled(isPackageInstalled: (String) -> Boolean): Boolean =
    listOf("com.nextcloud.client", "com.nextcloud.android.qa", "com.nextcloud.android.beta").any(isPackageInstalled)
