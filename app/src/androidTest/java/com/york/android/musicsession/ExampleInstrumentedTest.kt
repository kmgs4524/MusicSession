package com.york.android.musicsession

import android.graphics.Bitmap
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import com.york.android.musicsession.model.bitmap.GetBitmapFromUrl
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import kotlin.reflect.KClass

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.york.android.musicsession", appContext.packageName)
    }

    @Test
    fun getBitmapFromUrl() {
        val getBitmapFromUrl = GetBitmapFromUrl()

        assertThat(getBitmapFromUrl.getBitmap("https://i.kfs.io/artist/global/1210236,0v8/fit/160x160.jpg")!!, `is`(instanceOf(Bitmap::class.java)))
    }

    fun UiTest() {

    }
}
