package com.kindred.core.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Firebase bindings (Auth, Firestore, Storage) land here in Phase 1.
 * Exists now so the Hilt + KSP pipeline is proven from day one.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule
