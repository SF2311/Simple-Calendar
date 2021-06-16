package com.simplemobiletools.calendar.pro.models

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.simplemobiletools.calendar.pro.extensions.eventsDB
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import java.io.File
import java.io.Serializable
import java.net.URI

@Entity(tableName = "web_feeds", indices = [(Index(value = ["feedId"], unique = true))])
class WebCalendarFeed(
    @PrimaryKey(autoGenerate = true) var feedId: Long?,
    @ColumnInfo(name = "url") var feedUrl: String,
    @ColumnInfo(name = "name") var feedName: String = "",
    @ColumnInfo(name = "sync") var syncronizeFeed: Boolean = true,
    @ColumnInfo(name = "event_type") var eventTypeId: Long = -1L,
    @ColumnInfo(name = "cal_id") var calendarId: Int = 0,
    @ColumnInfo(name = "override_event_types") var overrideFileEventTypes: Boolean,
    @ColumnInfo(name = "last_sync") var lastSynchronized: Long = 0,
    @ColumnInfo(name = "keep") var keepPast: Boolean = false
) : Serializable {

    fun downloadFeed(applicationContext: Context): String? {
        var feedCache = File(applicationContext.cacheDir, "$feedId.ics")
        if (feedCache.exists()) {
            if (!downloadFeedToCache(feedCache)) {
                return null
            }
            return feedCache.absolutePath
        } else {
            feedCache = File.createTempFile("$feedId.ics", null, applicationContext.cacheDir)
            if (!downloadFeedToCache(feedCache)) {
                return null
            }
            return feedCache.absolutePath
        }
    }

    fun delete(applicationContext: Context) {
        ensureBackgroundThread {
            applicationContext.eventsDB.deleteEventsOfFeed(this.feedId!!)
            applicationContext.webCalendarFeedDB.delete(this)
        }
    }

    private fun downloadFeedToCache(cacheFile: File): Boolean {
        var result = false
        try {
            val downloadStream = URI.create(feedUrl).toURL().openStream().buffered()
            val fileStream = cacheFile.outputStream().buffered()
            downloadStream.copyTo(fileStream)
            downloadStream.close()
            fileStream.close()
            result = true
        } catch (e: Exception) {
            Log.e("downloadFeedToCache", e.toString())
            result = false
        }
        return result
    }
}
