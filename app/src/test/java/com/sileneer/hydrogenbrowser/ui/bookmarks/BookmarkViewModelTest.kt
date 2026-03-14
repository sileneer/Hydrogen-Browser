package com.sileneer.hydrogenbrowser.ui.bookmarks

import com.sileneer.hydrogenbrowser.data.BookmarkEntry
import com.sileneer.hydrogenbrowser.data.BookmarkRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: BookmarkRepository
    private lateinit var viewModel: BookmarkViewModel

    private val folder = BookmarkEntry(id = 10, title = "Folder", isFolder = true, parentId = null, position = 0, createdAt = 1000L)
    private val sampleBookmarks = listOf(
        folder,
        BookmarkEntry(id = 1, title = "Google", url = "https://google.com", isFolder = false, parentId = null, position = 1, createdAt = 2000L),
    )
    private val folderChildren = listOf(
        BookmarkEntry(id = 2, title = "GitHub", url = "https://github.com", isFolder = false, parentId = 10, position = 0, createdAt = 3000L),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getChildrenOf(null) } returns flowOf(sampleBookmarks)
        every { repository.getChildrenOf(10L) } returns flowOf(folderChildren)
        every { repository.searchBookmarks(any()) } returns flowOf(
            listOf(sampleBookmarks[1])
        )
        every { repository.getAllFolders() } returns flowOf(listOf(folder))
        viewModel = BookmarkViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is root folder`() {
        assertNull(viewModel.currentFolder.value)
        assertTrue(viewModel.folderPath.value.isEmpty())
    }

    @Test
    fun `children emits root items`() = runTest {
        val items = viewModel.children.first()
        assertEquals(sampleBookmarks, items)
    }

    @Test
    fun `navigateToFolder updates current folder and path`() = runTest {
        viewModel.navigateToFolder(folder)
        assertEquals(folder, viewModel.currentFolder.value)
        assertEquals(listOf(folder), viewModel.folderPath.value)
        val items = viewModel.children.first()
        assertEquals(folderChildren, items)
    }

    @Test
    fun `navigateUp returns to parent`() = runTest {
        viewModel.navigateToFolder(folder)
        val result = viewModel.navigateUp()
        assertTrue(result)
        assertNull(viewModel.currentFolder.value)
        assertTrue(viewModel.folderPath.value.isEmpty())
    }

    @Test
    fun `navigateUp at root returns false`() {
        val result = viewModel.navigateUp()
        assertFalse(result)
    }

    @Test
    fun `search filters bookmarks globally`() = runTest {
        viewModel.onSearchQueryChanged("Google")
        viewModel.toggleSearch()
        val items = viewModel.children.first()
        assertEquals(1, items.size)
        assertEquals("Google", items[0].title)
    }

    @Test
    fun `toggleSearch enters and exits search mode`() {
        viewModel.toggleSearch()
        assertTrue(viewModel.isSearching.value)
        viewModel.toggleSearch()
        assertFalse(viewModel.isSearching.value)
    }

    @Test
    fun `deleteEntry calls repository`() {
        val entry = sampleBookmarks[1]
        viewModel.deleteEntry(entry)
        coVerify { repository.deleteEntry(entry.id) }
    }

    @Test
    fun `undoDelete calls reInsert`() {
        val entry = sampleBookmarks[1]
        viewModel.undoDelete(entry)
        coVerify { repository.reInsert(entry) }
    }

    @Test
    fun `createFolder calls repository`() {
        viewModel.createFolder("New Folder")
        coVerify { repository.createFolder("New Folder", null) }
    }

    @Test
    fun `createFolder in subfolder uses current parentId`() = runTest {
        viewModel.navigateToFolder(folder)
        viewModel.createFolder("Sub Folder")
        coVerify { repository.createFolder("Sub Folder", 10L) }
    }
}
