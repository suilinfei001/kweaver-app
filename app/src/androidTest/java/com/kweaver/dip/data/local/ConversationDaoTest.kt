package com.kweaver.dip.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kweaver.dip.data.model.ConversationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ConversationDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.conversationDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndRead() = runTest {
        val conversation = ConversationEntity(title = "Test Chat")
        val id = dao.insert(conversation)
        val result = dao.getById(id)
        assertNotNull(result)
        assertEquals("Test Chat", result?.title)
    }

    @Test
    fun getAllOrderedByUpdatedTime() = runTest {
        dao.insert(ConversationEntity(title = "First", createdAt = 1000, updatedAt = 1000))
        dao.insert(ConversationEntity(title = "Second", createdAt = 2000, updatedAt = 2000))
        dao.insert(ConversationEntity(title = "Third", createdAt = 3000, updatedAt = 500))

        val all = dao.getAll().first()
        assertEquals(3, all.size)
        assertEquals("Second", all[0].title)
        assertEquals("First", all[1].title)
        assertEquals("Third", all[2].title)
    }

    @Test
    fun deleteConversation() = runTest {
        val id = dao.insert(ConversationEntity(title = "To Delete"))
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun updateTimestamp() = runTest {
        val id = dao.insert(ConversationEntity(title = "Test", updatedAt = 1000))
        dao.updateTimestamp(id, updatedAt = 5000)
        val result = dao.getById(id)
        assertEquals(5000L, result?.updatedAt)
    }
}
