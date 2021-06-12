package com.simplemobiletools.calendar.pro.objects

import android.content.Context
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.extensions.eventTypesDB
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.calendar.pro.helpers.Formatter
import com.simplemobiletools.calendar.pro.helpers.IcsImporter
import com.simplemobiletools.commons.extensions.toast
import org.joda.time.DateTime
import org.joda.time.Minutes
import java.time.format.DateTimeFormatter

object WebFeedSynchronizer {
    fun synchronizeWebFeeds(activity : SimpleActivity, applicationContext : Context, callback: (refreshView: Boolean) -> Unit){
        val webFeeds = activity.webCalendarFeedDB.getAll()
        var curPath : String? = ""
        for(feed in webFeeds){
            if ((Minutes.minutesBetween(DateTime(feed.lastSynchronized),DateTime())
            .isLessThan(Minutes.minutes(30))) || feed.syncronizeFeed ){
                continue
            }
            curPath = feed.downloadFeed(applicationContext)
            if(curPath != null){
                feed.lastSynchronized = DateTime.now().millis
                activity.webCalendarFeedDB.insertOrUpdate(feed)
                val eventType = activity.eventTypesDB.getEventTypeWithId(feed.eventTypeId)
                if(eventType != null) {
                    val result = IcsImporter(activity).importEvents(
                        curPath,
                        eventType.id!!,
                        eventType.caldavCalendarId,
                        feed.overrideFileEventTypes
                    )
                    activity.toast(
                        when (result) {
                            IcsImporter.ImportResult.IMPORT_NOTHING_NEW -> R.string.no_new_items
                            IcsImporter.ImportResult.IMPORT_OK -> R.string.importing_successful
                            IcsImporter.ImportResult.IMPORT_PARTIAL -> R.string.importing_some_entries_failed
                            else -> R.string.no_items_found
                        }
                    )
                    callback(result != IcsImporter.ImportResult.IMPORT_FAIL)
                }
            }
        }
    }

}
