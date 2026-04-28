package com.kweaver.dip.di

import com.kweaver.dip.data.api.DipHubApi
import com.kweaver.dip.data.api.DipStudioApi
import com.kweaver.dip.data.api.OAuth2LoginHelper
import com.kweaver.dip.data.api.SseClient
import com.kweaver.dip.data.local.datastore.TokenDataStore
import com.kweaver.dip.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        oAuth2LoginHelper: OAuth2LoginHelper,
        tokenDataStore: TokenDataStore
    ): AuthRepository = AuthRepository(oAuth2LoginHelper, tokenDataStore)

    @Provides
    @Singleton
    fun provideDigitalHumanRepository(
        dipStudioApi: DipStudioApi,
        tokenDataStore: TokenDataStore
    ): DigitalHumanRepository = DigitalHumanRepository(dipStudioApi, tokenDataStore)

    @Provides
    @Singleton
    fun provideChatRepository(
        dipStudioApi: DipStudioApi,
        sseClient: SseClient,
        tokenDataStore: TokenDataStore
    ): ChatRepository = ChatRepository(dipStudioApi, sseClient, tokenDataStore)

    @Provides
    @Singleton
    fun provideSessionRepository(
        dipStudioApi: DipStudioApi,
        tokenDataStore: TokenDataStore
    ): SessionRepository = SessionRepository(dipStudioApi, tokenDataStore)

    @Provides
    @Singleton
    fun provideSkillRepository(
        dipStudioApi: DipStudioApi,
        tokenDataStore: TokenDataStore
    ): SkillRepository = SkillRepository(dipStudioApi, tokenDataStore)

    @Provides
    @Singleton
    fun providePlanRepository(
        dipStudioApi: DipStudioApi,
        tokenDataStore: TokenDataStore
    ): PlanRepository = PlanRepository(dipStudioApi, tokenDataStore)

    @Provides
    @Singleton
    fun provideGuideRepository(
        dipStudioApi: DipStudioApi
    ): GuideRepository = GuideRepository(dipStudioApi)
}
