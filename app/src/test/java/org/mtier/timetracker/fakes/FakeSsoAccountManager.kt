package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.auth.SsoAccountManager

class FakeSsoAccountManager : SsoAccountManager {
    var cleared = false
        private set

    var clearError: Throwable? = null

    override fun clearAccount() {
        clearError?.let { throw it }
        cleared = true
    }
}
