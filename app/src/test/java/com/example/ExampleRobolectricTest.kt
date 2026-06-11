package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Aura Planner", appName)
  }

  @Test
  fun `gregorian to jalali conversion`() {
    // 2026-06-11 should be 1405-03-21 (21 Khordad 1405)
    val result = com.example.util.CalendarHelper.gregorianToJalali(2026, 6, 11)
    assertEquals(1405, result[0])
    assertEquals(3, result[1])
    assertEquals(21, result[2])

    // jalaliToGregorian on 1405-03-21 should be 2026-06-11
    val gregResult = com.example.util.CalendarHelper.jalaliToGregorian(1405, 3, 21)
    assertEquals(2026, gregResult[0])
    assertEquals(6, gregResult[1])
    assertEquals(11, gregResult[2])
  }
}
