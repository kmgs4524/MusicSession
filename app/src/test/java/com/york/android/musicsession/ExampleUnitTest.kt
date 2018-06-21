package com.york.android.musicsession

import android.content.Context
import android.graphics.Bitmap
import android.test.suitebuilder.annotation.SmallTest
import android.util.Log
import com.york.android.musicsession.model.bitmap.GetBitmapFromUrl
import com.york.android.musicsession.model.datafactory.SongFactory
import com.york.android.musicsession.model.jsonconverter.ArtistConverter
import com.york.android.musicsession.presenter.songpage.SongPagePresenter
import com.york.android.musicsession.view.songpage.SongPageView
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class ExampleUnitTest {
    @Mock
    var mockContext: Context? = null

    @Test
    fun addition_isCorrect() {
        val num1 = 3
        val num2 = 4
        add(num1, num2)
        assertEquals(7, add(num1, num2))
    }

    fun add(num1: Int, num2: Int): Int {
        return num1 + num2
    }

    @Test
    fun convertJson() {
        val convertor = ArtistConverter()
        val jsonString = "{\"artists\":{\"data\":[{\"id\":\"HZijtr0zUOvYSN8JCC\",\"name\":\"\\u5433\\u6c76\\u82b3 (Fang Wu)\",\"url\":\"https:\\/\\/event.kkbox.com\\/content\\/artist\\/HZijtr0zUOvYSN8JCC\",\"images\":[{\"height\":160,\"width\":160,\"url\":\"https:\\/\\/i.kfs.io\\/artist\\/global\\/1210236,0v8\\/fit\\/160x160.jpg\"},{\"height\":300,\"width\":300," +
                "\"url\":\"https:\\/\\/i.kfs.io\\/artist\\/global\\/1210236,0v8\\/fit\\/300x300.jpg\"}]},{\"id\":\"9ZqOgMi6Jj_jLJBrKh\",\"name\":\"\\u66fe\\u975c\\u739f+\\u5433\\u6c76\\u82b3 (Jing Wen Tseng+Fang Wu)\",\"url\":\"https:\\/\\/event.kkbox.com\\/content\\/artist\\/9ZqOgMi6Jj_jLJBrKh\",\"images\":[{\"height\":160,\"width\":160,\"url\":\"https:\\/\\/i.kfs.io\\" +
                "/artist\\/global\\/3916845,0v1\\/fit\\/160x160.jpg\"},{\"height\":300,\"width\":300,\"url\":\"https:\\/\\/i.kfs.io\\/artist\\/global\\/3916845,0v1\\/fit\\/300x300.jpg\"}]}],\"paging\":{\"offset\":0,\"limit\":15,\"previous\":null,\"next\":null},\"summary\":{\"total\":2}},\"paging\":{\"offset\":0,\"limit\":15,\"previous\":null,\"next\":null},\"summary\":{\"total\":2}}"

        assertEquals("https://i.kfs.io/artist/global/1210236,0v8/fit/160x160.jpg", convertor.convertToImageUrl(jsonString))
    }

    @Test
    fun testGetSongs() {
        val mockSongFactory = Mockito.mock(SongFactory::class.java)
        val mockSongPageView = Mockito.mock(SongPageView::class.java)
        val songPagePresenter = SongPagePresenter(mockSongPageView, mockSongFactory)

        songPagePresenter.onVerifyPermission()
        // verify calling songFactory.getSongs() once
        Mockito.verify(mockSongFactory).getSongs("", "")
        Mockito.verify(mockSongPageView).initRecyclerView(mockSongFactory.getSongs("", ""))
    }

}
