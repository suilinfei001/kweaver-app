package com.kweaver.dip.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kweaver.dip.data.model.ConversationEntity
import com.kweaver.dip.data.model.MessageEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MessageDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var conversationDao: ConversationDao
    private var conversationId: Long = 0

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        messageDao = db.messageDao()
        conversationDao = db.conversationDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    private suspend fun createConversation(): Long {
        return conversationDao.insert(ConversationEntity(title = "Test"))
    }

    @Test
    fun insertAndGetByConversation() = runTest {
        conversationId = createConversation()
        messageDao.insert(MessageEntity(conversationId = conversationId, role = "user", content = "Hello"))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = "assistant", content = "Hi there"))

        val messages = messageDao.getByConversation(conversationId).first()
        assertEquals(2, messages.size)
        assertEquals("Hello", messages[0].content)
        assertEquals("Hi there", messages[1].content)
    }

    @Test
    fun messagesOrderedByTimestamp() = runTest {
        conversationId = createConversation()
        messageDao.insert(MessageEntity(conversationId = conversationId, role = "user", content = "Second", timestamp = 2000))
        messageDao.insert(MessageEntity(conversationId = conversationId, role = "user", content = "First", timestamp = 1000))

        val messages = messageDao.getByConversation(conversationId).first()
        assertEquals("First", messages[0].content)
        assertEquals("Second", messages[1].content)
    }

    @Test
    fun updateStatus() = runTest {
        conversationId = createConversation()
        val msgId = messageDao.insert(
            MessageEntity(conversationId = conversationId, role = "assistant", content = "Hello", status = "sending")
        )
        messageDao.updateStatus(msgId, "sent")

        val messages = messageDao.getByConversation(conversationId).first()
        assertEquals("sent", messages[0].status)
    }

    @Test
    fun getRecentByConversation() = runTest {
        conversationId = createConversation()
        for (i in 1..5) {
            messageDao.insert(MessageEntity(conversationId = conversationId, role = "user", content = "Msg $i", timestamp = i.toLong()))
        }
        val recent = messageDao.getRecentByConversation(conversationId, limit = 3)
        assertEquals(3, recent.size)
        assertEquals("Msg 5", recent[0].content)
        assertEquals("Msg 3", recent[2].content)
    }

    @Test
    fun deleteByConversation() = runTest {
        conversationId = createConversation()
        messageDao.insert(MessageEntity(conversationId = conversationId, role = "user", content = "Hello"))
        messageDao.deleteByConversation(conversationId)

        val messages = messageDao.getByConversation(conversationId).first()
        assertEquals(0, messages.size)
    }

    @Test
    fun updateContentAndStatus() = runTest {
        conversationId = createConversation()
        val msgId = messageDao.insert(
            MessageEntity(conversationId = conversationId, role = "assistant", content = "", status = "sending")
        )
        messageDao.updateContentAndStatus(msgId, "Full response text", "sent")

        val messages = messageDao.getByConversation(conversationId).first()
        assertEquals("Full response text", messages[0].content)
        assertEquals("sent", messages[0].status)
    }
}
