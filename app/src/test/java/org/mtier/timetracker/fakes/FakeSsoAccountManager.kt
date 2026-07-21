package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.auth.SsoAccountManager

class FakeSsoAccountManager : SsoAccountManager {
    var cleared = false
        private set

    override fun clearAccount() {
        cleared = true
    }
}
