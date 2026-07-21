package org.mtier.timetracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.mtier.timetracker.data.auth.CredentialStorage
import org.mtier.timetracker.data.auth.CredentialStore

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindCredentialStorage(impl: CredentialStore): CredentialStorage
}
