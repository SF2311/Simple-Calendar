package com.simplemobiletools.calendar.pro.activities

import android.content.Intent
import android.os.Bundle
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.dialogs.WebFeedEditDialog
import com.simplemobiletools.calendar.pro.helpers.IcsImporter
import com.simplemobiletools.calendar.pro.helpers.STORED_LOCALLY_ONLY
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.activity_web_feed.*

class WebFeedActivity : SimpleActivity() {
    //TODO: ability to delete existing webfeeds
    //TODO: manual sync
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_feed)
        setupMenuElements()
    }

    override fun onResume() {
        super.onResume()
        setupMenuElements()
    }

    private fun setupMenuElements(){
        webfeed_settings_subscribe_wrapper.setOnClickListener {
            startWebFeedSubscription()
        }
        webfeed_settings_edit_wrapper.setOnClickListener {
            startWebFeedEdit()
        }
    }

    private fun startWebFeedSubscription(){
        WebFeedEditDialog(this, null){
            val webFeed = it
            ensureBackgroundThread {
                val filePath = webFeed.downloadFeed(applicationContext)
                if (filePath != null) {
                    IcsImporter(this).importEvents(
                        filePath,
                        webFeed.eventTypeId,
                        STORED_LOCALLY_ONLY,
                        webFeed.overrideFileEventTypes,
                        webFeed.feedId!!
                    )
                    runOnUiThread {
                        toast(("Successfully imported feed"))
                    }
                } else {
                    runOnUiThread {
                        toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
    private fun startWebFeedEdit(){
        startActivity(Intent(applicationContext, FeedEditActivity::class.java))
    }
}
