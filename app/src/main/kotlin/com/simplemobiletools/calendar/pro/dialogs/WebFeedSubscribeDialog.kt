package com.simplemobiletools.calendar.pro.dialogs

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.extensions.config
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.calendar.pro.helpers.IcsImporter
import com.simplemobiletools.calendar.pro.models.EventType
import com.simplemobiletools.calendar.pro.models.WebCalendarFeed
import com.simplemobiletools.commons.extensions.getCornerRadius
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.setFillWithStroke
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.dialog_import_events.view.*
import kotlinx.android.synthetic.main.dialog_web_feed_url.*
import kotlinx.android.synthetic.main.dialog_web_feed_url.view.*
import org.joda.time.DateTime
import java.net.URL

class WebFeedSubscribeDialog(val activity: SimpleActivity, val callback: (refreshView: Boolean) -> Unit) {
    val config = activity.config

    init {

        activity.runOnUiThread {
            initDialog()
        }
    }

    private fun initDialog() {
        //TODO: check Network state (allow mobile downloads?)
        var eventType = EventType(null, "", activity.config.primaryColor)
        val view =
            (activity.layoutInflater.inflate(R.layout.dialog_web_feed_url, null) as ViewGroup).apply{
                updateEventType(this, eventType)
                webfeed_type_holder.setOnClickListener {
                    EditEventTypeDialog(activity) {
                        eventType = it
                        updateEventType(this, eventType)
                        activity.hideKeyboard()
                    }
                }
            }
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.enter_feed_url) {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if(eventType.id == null){
                            webfeed_error.text = activity.getString(R.string.webfeed_choose_event_type)
                            webfeed_error.visibility = View.VISIBLE
                            return@setOnClickListener
                        }
                        try {
                            URL(webfeed_url.text.toString()).toURI()
                            Log.d("WebFeedSubscribeDialog", webfeed_url.text.toString())
                        }catch(e : Exception) {
                            webfeed_error.text = activity.getString(R.string.enter_valid_url)
                            webfeed_error.visibility = View.VISIBLE
                            return@setOnClickListener
                        }
                            val webFeed = WebCalendarFeed(
                                null,
                                webfeed_url.text.toString(),
                                webfeed_checkbox_synchronize.isChecked,
                                eventType.id!!,
                                webfeed_checkbox_override.isChecked,
                                DateTime.now().millis
                            )

                           ensureBackgroundThread {
                               webFeed.feedId = activity.webCalendarFeedDB.insertOrUpdate(webFeed)
                               val filePath = webFeed.downloadFeed(activity.applicationContext)
                               if (filePath != null) {
                                   IcsImporter(activity).importEvents(
                                       filePath,
                                       eventType.id!!,
                                       eventType.caldavCalendarId,
                                       webfeed_checkbox_override.isChecked
                                   )
                               } else {
                                   activity.runOnUiThread {
                                       webfeed_error.text =
                                           activity.getString(R.string.unknown_error_occurred)
                                       webfeed_error.visibility = View.VISIBLE
                                   }
                               }
                           }

                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(null)
                        ensureBackgroundThread {
                            dismiss()
                        }
                    }
                }
            }
    }

    private fun updateEventType(view: ViewGroup, eventType : EventType) {
        ensureBackgroundThread {
            activity.runOnUiThread {
                view.webfeed_type_title.text = eventType!!.getDisplayTitle()
                view.webfeed_type_color.setFillWithStroke(eventType.color, activity.config.backgroundColor, activity.getCornerRadius())
            }
        }
    }
}
