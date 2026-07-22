package org.mtier.timetracker.fakes

import org.mtier.timetracker.data.auth.CredentialStorage
import org.mtier.timetracker.data.auth.Credentials

class FakeCredentialStore : CredentialStorage {
    private var stored: Credentials? = null

    override fun save(credentials: Credentials) {
        stored = credentials
    }

    override fun load(): Credentials? = stored

    override fun clear() {
        stored = null
    }
}
