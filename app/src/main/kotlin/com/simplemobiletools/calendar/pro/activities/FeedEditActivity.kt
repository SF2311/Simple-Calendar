package com.simplemobiletools.calendar.pro.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplemobiletools.calendar.pro.R
import com.simplemobiletools.calendar.pro.adapters.WebCalendarFeedAdapter
import com.simplemobiletools.calendar.pro.extensions.webCalendarFeedDB
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import kotlinx.android.synthetic.main.activity_feed_edit.*

class FeedEditActivity : SimpleActivity() {
    lateinit var adapter: WebCalendarFeedAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed_edit)
        ensureBackgroundThread {
            adapter = WebCalendarFeedAdapter(this, webCalendarFeedDB.getAll().toTypedArray())
            if(adapter.webFeeds.isEmpty()){
                empty_view.visibility = View.VISIBLE
            }
            webfeed_edit_recycler.adapter = adapter
        }
        webfeed_edit_recycler.layoutManager = LinearLayoutManager(this)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when(item.title){
            getString(R.string.delete) -> deleteFeed(item.groupId)
        }
        return false
    }

    private fun deleteFeed(feedPosition : Int){
        adapter.webFeeds[feedPosition].delete(this.applicationContext)
        toast(R.string.delete_feed_success, Toast.LENGTH_LONG)
    }
}
