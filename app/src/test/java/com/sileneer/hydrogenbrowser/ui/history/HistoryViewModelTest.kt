package com.sileneer.hydrogenbrowser.ui.history

import com.sileneer.hydrogenbrowser.data.HistoryEntry
import com.sileneer.hydrogenbrowser.data.HistoryRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: HistoryRepository
    private lateinit var viewModel: HistoryViewModel

    private val sampleEntries = listOf(
        HistoryEntry(id = 1, url = "https://google.com", title = "Google", timestamp = 1000L),
        HistoryEntry(id = 2, url = "https://github.com", title = "GitHub", timestamp = 2000L),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getAllHistory() } returns flowOf(sampleEntries)
        every { repository.searchHistory(any()) } returns flowOf(
            sampleEntries.filter { it.title.contains("Google", ignoreCase = true) }
        )
        viewModel = HistoryViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial search query is empty`() {
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `onSearchQueryChanged updates query`() {
        viewModel.onSearchQueryChanged("test")
        assertEquals("test", viewModel.searchQuery.value)
    }

    @Test
    fun `historyEntries emits all entries`() = runTest {
        val entries = viewModel.historyEntries.first()
        assertEquals(sampleEntries, entries)
    }

    @Test
    fun `search filters entries`() = runTest {
        viewModel.onSearchQueryChanged("Google")
        val entries = viewModel.historyEntries.first()
        assertEquals(1, entries.size)
        assertEquals("Google", entries[0].title)
    }

    @Test
    fun `deleteEntry calls repository`() {
        viewModel.deleteEntry(1L)
        coVerify { repository.deleteEntry(1L) }
    }

    @Test
    fun `clearAllHistory calls repository`() {
        viewModel.clearAllHistory()
        coVerify { repository.clearAll() }
    }

    @Test
    fun `undoDelete re-inserts entry`() {
        val entry = sampleEntries[0]
        viewModel.undoDelete(entry)
        coVerify { repository.reInsert(entry) }
    }
}
