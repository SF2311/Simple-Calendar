package com.simplemobiletools.calendar.pro.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.adapters.WebCalendarFeedAdapter
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.activity_feed_edit.*

class FeedEditActivity : SimpleActivity() {
    lateinit var adapter: WebCalendarFeedAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_edit)
        ensureBackgroundThread {
            adapter = WebCalendarFeedAdapter(this, webCalendarFeedDB.getAll().toTypedArray())
            webfeed_edit_recycler.adapter = adapter
        }
        webfeed_edit_recycler.layoutManager = LinearLayoutManager(this)


    }
}
