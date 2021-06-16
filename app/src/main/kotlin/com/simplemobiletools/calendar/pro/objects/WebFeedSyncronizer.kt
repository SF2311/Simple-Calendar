package com.simplemobiletools.calendar.pro.objects

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.extensions.*
import com.simplemobiletools.calendar.pro.helpers.Config
import com.simplemobiletools.calendar.pro.helpers.IcsImporter
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import org.joda.time.DateTime
import org.joda.time.Minutes

object WebFeedSynchronizer {
    fun synchronizeWebFeeds(
        activity: SimpleActivity,
        showToast: Boolean = true,
        manualSync: Boolean = false,
        callback: (refreshView: Boolean) -> Unit
    ) {
        if(!checkNetwork(activity.applicationContext)){
            activity.toast(R.string.mobile_download_not_allowed)
            return
        }
        ensureBackgroundThread {
            val applicationContext = activity.applicationContext
            val webFeeds = activity.webCalendarFeedDB.getAll()
            var curPath: String? = ""
            for (feed in webFeeds) {
                if (((Minutes.minutesBetween(DateTime(feed.lastSynchronized), DateTime())
                        .isLessThan(Minutes.minutes(30))) && !manualSync) || !feed.syncronizeFeed
                ) {
                    continue
                }
                curPath = feed.downloadFeed(applicationContext)
                if (curPath != null) {
                    feed.lastSynchronized = DateTime.now().millis
                    activity.webCalendarFeedDB.insertOrUpdate(feed)
                    val eventType = activity.eventTypesDB.getEventTypeWithId(feed.eventTypeId)
                    if (eventType != null) {
                        val result = IcsImporter(activity).importEvents(
                            curPath,
                            eventType.id!!,
                            eventType.caldavCalendarId,
                            feed.overrideFileEventTypes,
                            feed.feedId!!
                        ) { parsedEvents ->
                            ensureBackgroundThread {
                                val feedEvents = activity.eventsDB.getEventsOfFeed(feed.feedId!!)
                                feedEvents.forEach { feedEvent ->
                                    if (parsedEvents.find { it.id == feedEvent.id } == null) {
                                        if (!feedEvent.isPastEvent) {
                                            applicationContext.eventsHelper.deleteEvent(
                                                feedEvent.id!!,
                                                true
                                            )
                                        } else {
                                            if (!feed.keepPast) {
                                                applicationContext.eventsHelper.deleteEvent(
                                                    feedEvent.id!!,
                                                    true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (showToast) {
                            activity.toast(
                                when (result) {
                                    IcsImporter.ImportResult.IMPORT_NOTHING_NEW -> R.string.no_new_items
                                    IcsImporter.ImportResult.IMPORT_OK -> R.string.importing_successful
                                    IcsImporter.ImportResult.IMPORT_PARTIAL -> R.string.importing_some_entries_failed
                                    else -> R.string.no_items_found
                                }
                            )
                        }
                        callback(result != IcsImporter.ImportResult.IMPORT_FAIL)
                    }
                }
            }
        }
    }

    private fun checkNetwork(context: Context) : Boolean{
        if(context.config.allowMobileDownloads){
            return true
        }else{
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return !connMgr.isActiveNetworkMetered
        }
    }

}
