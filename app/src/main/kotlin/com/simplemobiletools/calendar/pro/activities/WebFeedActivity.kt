package com.simplemobiletools.calendar.pro.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.dialogs.WebFeedSubscribeDialog
import com.simplemobiletools.commons.extensions.toast
import kotlinx.android.synthetic.main.activity_web_feed.*

class WebFeedActivity : SimpleActivity() {
    //TODO: menu to configure existing webfeeds
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
    }

    private fun startWebFeedSubscription(){
        WebFeedSubscribeDialog(this){

        }
    }
}
