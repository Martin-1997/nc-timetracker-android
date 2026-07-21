package org.mtier.timetracker.data.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NextcloudSsoManagerTest {
    @Test
    fun `detects the production Files app package`() {
        assertTrue(isFilesAppInstalled { pkg -> pkg == "com.nextcloud.client" })
    }

    @Test
    fun `detects the QA or beta Files app package too`() {
        assertTrue(isFilesAppInstalled { pkg -> pkg == "com.nextcloud.android.qa" })
        assertTrue(isFilesAppInstalled { pkg -> pkg == "com.nextcloud.android.beta" })
    }

    @Test
    fun `false when none of the known packages are installed`() {
        assertFalse(isFilesAppInstalled { false })
    }
}
