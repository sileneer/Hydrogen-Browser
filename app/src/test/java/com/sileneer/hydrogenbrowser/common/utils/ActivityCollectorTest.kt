package com.sileneer.hydrogenbrowser.common.utils

import android.app.Activity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ActivityCollectorTest {

    @Before
    fun setUp() {
        ActivityCollector.activities.clear()
    }

    @After
    fun tearDown() {
        ActivityCollector.activities.clear()
    }

    @Test
    fun `addActivity adds to list`() {
        val activity = mockk<Activity>(relaxed = true)
        ActivityCollector.addActivity(activity)
        assertEquals(1, ActivityCollector.activities.size)
        assertTrue(ActivityCollector.activities.contains(activity))
    }

    @Test
    fun `removeActivity removes from list`() {
        val activity = mockk<Activity>(relaxed = true)
        ActivityCollector.addActivity(activity)
        ActivityCollector.removeActivity(activity)
        assertEquals(0, ActivityCollector.activities.size)
    }

    @Test
    fun `finishAll finishes non-finishing activities`() {
        val active = mockk<Activity>(relaxed = true)
        val finishing = mockk<Activity>(relaxed = true)
        every { active.isFinishing } returns false
        every { finishing.isFinishing } returns true

        ActivityCollector.addActivity(active)
        ActivityCollector.addActivity(finishing)
        ActivityCollector.finishAll()

        verify(exactly = 1) { active.finish() }
        verify(exactly = 0) { finishing.finish() }
    }
}
