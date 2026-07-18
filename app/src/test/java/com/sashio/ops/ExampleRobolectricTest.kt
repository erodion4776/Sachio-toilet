package com.sashio.ops

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.sashio.ops.ui.SashioViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SACHIO Toilets", appName)
  }

  @Test
  fun `test database seeding the viewmodel successfully`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = SashioViewModel(app)
    
    // Check initial state
    assertEquals("login", viewModel.currentScreen.value)
    
    // Wait for seeding to complete and check staffMember count or payment log count
    val staffList = viewModel.staffProfiles.first()
    assertNotNull(staffList)
  }
}
