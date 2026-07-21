package org.mtier.timetracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mtier.timetracker.data.auth.CredentialStorage
import org.mtier.timetracker.data.auth.CredentialStore
import org.mtier.timetracker.data.auth.NextcloudSsoManager
import org.mtier.timetracker.data.auth.SsoAccountManager
import org.mtier.timetracker.data.auth.SsoLoginManager

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindCredentialStorage(impl: CredentialStore): CredentialStorage

    @Binds
    abstract fun bindSsoAccountManager(impl: NextcloudSsoManager): SsoAccountManager

    @Binds
    abstract fun bindSsoLoginManager(impl: NextcloudSsoManager): SsoLoginManager
}
