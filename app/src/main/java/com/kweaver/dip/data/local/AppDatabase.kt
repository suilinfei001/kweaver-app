package com.kweaver.dip.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kweaver.dip.data.model.ConversationEntity
import com.kweaver.dip.data.model.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
}
