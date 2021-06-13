package com.simplemobiletools.calendar.pro.dialogs

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.activities.SimpleActivity
import com.simplemobiletools.calendar.pro.extensions.config
import com.simplemobiletools.calendar.pro.extensions.eventTypesDB
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.calendar.pro.helpers.REGULAR_EVENT_TYPE_ID
import com.simplemobiletools.calendar.pro.models.Event
import com.simplemobiletools.calendar.pro.models.EventType
import com.simplemobiletools.calendar.pro.models.WebCalendarFeed
import com.simplemobiletools.commons.extensions.getCornerRadius
import com.simplemobiletools.commons.extensions.hideKeyboard
import com.simplemobiletools.commons.extensions.setFillWithStroke
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.views.MyAppCompatCheckbox
import kotlinx.android.synthetic.main.dialog_import_events.view.*
import kotlinx.android.synthetic.main.dialog_web_feed_edit.*
import kotlinx.android.synthetic.main.dialog_web_feed_edit.view.*
import org.joda.time.DateTime
import java.net.URL

class WebFeedEditDialog(val activity: SimpleActivity, val webFeed : WebCalendarFeed?, val callback: (feed : WebCalendarFeed) -> Unit) {
    val config = activity.config
    val createNewFeed = webFeed == null
    lateinit var eventType : EventType

    init {
        ensureBackgroundThread {
            eventType=
                if(createNewFeed) EventType(null, "", activity.config.primaryColor)
                else activity.eventTypesDB.getEventTypeWithId(
                    if(!webFeed!!.overrideFileEventTypes) REGULAR_EVENT_TYPE_ID
                    else webFeed!!.eventTypeId
                )!!
            activity.runOnUiThread {
                initDialog()
            }
        }
    }

    private fun initDialog() {

        val view =
            (activity.layoutInflater.inflate(R.layout.dialog_web_feed_edit, null) as ViewGroup).apply{
                updateEventType(this, eventType)
                if(!createNewFeed){
                    if(webFeed!!.overrideFileEventTypes){
                        webfeed_checkbox_override.isChecked = true
                        webfeed_event_selector_wrapper.visibility = View.VISIBLE
                    }
                    webfeed_name.setText(webFeed!!.feedName)
                    webfeed_url.setText(webFeed!!.feedUrl)
                    webfeed_checkbox_synchronize.isChecked = webFeed.syncronizeFeed
                }
                webfeed_type_holder.setOnClickListener {
                    SelectEventTypeDialog(activity, eventType.id!!, true, true, false, true) {
                        eventType = it
                        config.lastUsedLocalEventTypeId = it.id!!
                        config.lastUsedCaldavCalendarId = it.caldavCalendarId

                        updateEventType(this, eventType)
                    }
                }
            }
        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.enter_feed_url) {
                    webfeed_checkbox_override.setOnClickListener {
                        var checkBox = it as MyAppCompatCheckbox
                        if(checkBox.isChecked()){
                            webfeed_event_selector_wrapper.visibility = View.VISIBLE
                        }else{
                            webfeed_event_selector_wrapper.visibility = View.INVISIBLE
                        }
                    }
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if(eventType.id == null){
                            if(!webfeed_checkbox_override.isChecked){
                                eventType.id= REGULAR_EVENT_TYPE_ID
                            }else {
                                webfeed_error.text =
                                    activity.getString(R.string.webfeed_choose_event_type)
                                webfeed_error.visibility = View.VISIBLE
                                return@setOnClickListener
                            }
                        }
                        try {
                            URL(webfeed_url.text.toString()).toURI()
                            Log.d("WebFeedSubscribeDialog", webfeed_url.text.toString())
                        }catch(e : Exception) {
                            Log.e("Dialog.onClick()",e.toString())
                            webfeed_error.text = activity.getString(R.string.enter_valid_url)
                            webfeed_error.visibility = View.VISIBLE
                            return@setOnClickListener
                        }
                        ensureBackgroundThread {
                            val feeds = activity.webCalendarFeedDB.getByName(webfeed_name.text.toString())
                            if (feeds.isNotEmpty() && (createNewFeed || (feeds[0]).feedId!=webFeed!!.feedId)) {
                                activity.runOnUiThread {
                                    webfeed_error.text =
                                        activity.getString(R.string.enter_unique_feed_name)
                                    webfeed_error.visibility = View.VISIBLE
                                }
                            }else {

                                val webFeed = WebCalendarFeed(
                                    webFeed?.feedId,
                                    webfeed_url.text.toString(),
                                    webfeed_name.text.toString(),
                                    webfeed_checkbox_synchronize.isChecked,
                                    eventType.id!!,
                                    eventType.caldavCalendarId,
                                    webfeed_checkbox_override.isChecked,
                                    DateTime.now().millis
                                )
                                ensureBackgroundThread {
                                    webFeed.feedId =
                                        activity.webCalendarFeedDB.insertOrUpdate(webFeed)
                                }
                                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(null)
                                activity.runOnUiThread {
                                    dismiss()
                                    callback(webFeed)
                                }
                            }
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
