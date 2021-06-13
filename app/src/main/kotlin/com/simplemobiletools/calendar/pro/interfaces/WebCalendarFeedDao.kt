package com.simplemobiletools.calendar.pro.interfaces

import androidx.room.*
import com.simplemobiletools.calendar.pro.models.WebCalendarFeed

@Dao
interface WebCalendarFeedDao {
    @Query("SELECT * FROM web_feeds ORDER BY feedId ASC")
    fun getAll() : List<WebCalendarFeed>

    @Query("SELECT * FROM web_feeds WHERE feedId = :id")
    fun getById(id : Long) : WebCalendarFeed

    @Query("SELECT * FROM web_feeds WHERE name = :feedName")
    fun getByName(feedName: String) : List<WebCalendarFeed>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(feed : WebCalendarFeed) : Long

    @Delete
    fun delete(feed : WebCalendarFeed)
}
