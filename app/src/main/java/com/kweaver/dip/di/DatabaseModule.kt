package com.kweaver.dip.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kweaver.dip.data.local.AppDatabase
import com.kweaver.dip.data.local.ConversationDao
import com.kweaver.dip.data.local.MessageDao
import com.kweaver.dip.data.local.AiConfigDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_config")

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kweaver-chat.db").build()

    @Provides
    fun provideConversationDao(db: AppDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideAiConfigDataStore(dataStore: DataStore<Preferences>): AiConfigDataStore =
        AiConfigDataStore(dataStore)
}
