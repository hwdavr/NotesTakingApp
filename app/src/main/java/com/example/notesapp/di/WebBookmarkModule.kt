package com.example.notesapp.di

import com.example.notesapp.data.bookmark.HttpsWebBookmarkMetadataSource
import com.example.notesapp.domain.bookmark.WebBookmarkMetadataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the production bookmark metadata boundary in its own module so a test can uninstall the
 * real HTTPS client and install a deterministic fixture source without touching unrelated graph.
 */
@Module
@InstallIn(SingletonComponent::class)
object WebBookmarkModule {

    @Provides
    @Singleton
    fun provideWebBookmarkMetadataSource(): WebBookmarkMetadataSource = HttpsWebBookmarkMetadataSource()
}
