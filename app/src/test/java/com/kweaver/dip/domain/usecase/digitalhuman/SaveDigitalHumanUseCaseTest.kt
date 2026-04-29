package com.kweaver.dip.domain.usecase.digitalhuman

import com.kweaver.dip.data.model.*
import com.kweaver.dip.data.repository.DigitalHumanRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class SaveDigitalHumanUseCaseTest {

    @Mock
    private lateinit var digitalHumanRepository: DigitalHumanRepository

    private lateinit var saveUseCase: SaveDigitalHumanUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        saveUseCase = SaveDigitalHumanUseCase(digitalHumanRepository)
    }

    @Test
    fun `invoke validates name is required`() = runTest {
        val result = saveUseCase(
            id = null,
            name = "",
            creature = "",
            soul = "",
            bknEntries = emptyList(),
            selectedSkills = emptyList(),
            channelType = "",
            channelAppId = "",
            channelAppSecret = ""
        )

        assertTrue(result.isFailure)
        assertEquals("Name is required", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke creates new digital human when id is null`() = runTest {
        val expected = CreateDigitalHumanResult(id = "new-1", name = "Agent")
        whenever(digitalHumanRepository.createDigitalHuman(
            CreateDigitalHumanRequest(name = "Agent", creature = null, soul = null, bkn = null, skills = null, channel = null)
        )).thenReturn(Result.success(expected))

        val result = saveUseCase(
            id = null,
            name = "Agent",
            creature = "",
            soul = "",
            bknEntries = emptyList(),
            selectedSkills = emptyList(),
            channelType = "",
            channelAppId = "",
            channelAppSecret = ""
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke updates existing digital human when id is provided`() = runTest {
        val expected = DigitalHumanDetail(id = "1", name = "Updated")
        whenever(digitalHumanRepository.updateDigitalHuman(
            "1",
            UpdateDigitalHumanRequest(name = "Updated", creature = null, soul = null, bkn = null, skills = null, channel = null)
        )).thenReturn(Result.success(expected))

        val result = saveUseCase(
            id = "1",
            name = "Updated",
            creature = "",
            soul = "",
            bknEntries = emptyList(),
            selectedSkills = emptyList(),
            channelType = "",
            channelAppId = "",
            channelAppSecret = ""
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke filters blank bkn entries`() = runTest {
        val expected = CreateDigitalHumanResult(id = "1", name = "Agent")
        whenever(digitalHumanRepository.createDigitalHuman(
            CreateDigitalHumanRequest(
                name = "Agent",
                bkn = listOf(BknEntry("Valid", "http://example.com")),
                skills = null, creature = null, soul = null, channel = null
            )
        )).thenReturn(Result.success(expected))

        val result = saveUseCase(
            id = null,
            name = "Agent",
            creature = "",
            soul = "",
            bknEntries = listOf(
                BknEntry("Valid", "http://example.com"),
                BknEntry("", "")  // Should be filtered
            ),
            selectedSkills = emptyList(),
            channelType = "",
            channelAppId = "",
            channelAppSecret = ""
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke includes channel config when appId is provided`() = runTest {
        val expected = CreateDigitalHumanResult(id = "1", name = "Agent")
        whenever(digitalHumanRepository.createDigitalHuman(
            CreateDigitalHumanRequest(
                name = "Agent",
                channel = ChannelConfig(type = "wechat", appId = "wx123", appSecret = "secret"),
                skills = null, creature = null, soul = null, bkn = null
            )
        )).thenReturn(Result.success(expected))

        val result = saveUseCase(
            id = null,
            name = "Agent",
            creature = "",
            soul = "",
            bknEntries = emptyList(),
            selectedSkills = emptyList(),
            channelType = "wechat",
            channelAppId = "wx123",
            channelAppSecret = "secret"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure on repository error`() = runTest {
        whenever(digitalHumanRepository.createDigitalHuman(
            CreateDigitalHumanRequest(name = "Agent", creature = null, soul = null, bkn = null, skills = null, channel = null)
        )).thenReturn(Result.failure(RuntimeException("Server error")))

        val result = saveUseCase(
            id = null,
            name = "Agent",
            creature = "",
            soul = "",
            bknEntries = emptyList(),
            selectedSkills = emptyList(),
            channelType = "",
            channelAppId = "",
            channelAppSecret = ""
        )

        assertTrue(result.isFailure)
    }
}
