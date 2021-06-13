package com.simplemobiletools.calendar.pro.models

import android.content.Context
import android.util.Log
import androidx.room.*
import com.simplemobiletools.calendar.pro.activities.MainActivity
import java.io.File
import java.io.Serializable
import java.net.URI
import java.util.*
//TODO: tidy up database relations (foreign keys)
@Entity(tableName = "web_feeds", indices = [(Index(value = ["feedId"], unique = true))])
class WebCalendarFeed(@PrimaryKey(autoGenerate = true) var feedId : Long?,
                      @ColumnInfo(name= "url") var feedUrl: String,
                      @ColumnInfo(name="name") var feedName : String = "",
                      @ColumnInfo(name = "sync") var syncronizeFeed : Boolean = true,
                      @ColumnInfo(name="event_type") var eventTypeId : Long = -1L,
                      @ColumnInfo(name="cal_id") var calendarId : Int = 0,
                      @ColumnInfo(name="override_event_types") var overrideFileEventTypes : Boolean,
                      @ColumnInfo(name="last_sync") var lastSynchronized : Long = 0): Serializable {

    fun downloadFeed(applicationContext: Context) : String? {
        //TODO: check Network state (allow mobile downloads?)
        var feedCache = File(applicationContext.cacheDir, "$feedId.ics")
        if(feedCache.exists()){
            Log.d("downloadFeed", applicationContext.cacheDir.listFiles().sortedArray().contentToString())
            if(!downloadFeedToCache(feedCache)){
                Log.d("downloadFeed","failed")
                return null
            }
            return feedCache.absolutePath
        }else{
            feedCache = File.createTempFile("$feedId.ics", null, applicationContext.cacheDir)
            Log.d("downloadFeed", applicationContext.cacheDir.listFiles().sortedArray().contentToString())
            if(!downloadFeedToCache(feedCache)){
                Log.d("downloadFeed","failed")
                return null
            }
            return feedCache.absolutePath
        }
    }

    private fun downloadFeedToCache(cacheFile: File) : Boolean{
        var result = false
        try{
            val downloadStream = URI.create(feedUrl).toURL().openStream().buffered()
            val fileStream = cacheFile.outputStream().buffered()
            downloadStream.copyTo(fileStream)
            downloadStream.close()
            fileStream.close()
            result = true
        }catch (e: Exception){
            Log.e("downloadFeedToCache",e.toString())
            result = false
        }
        return result
    }
}
