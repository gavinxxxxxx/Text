package me.gavin.app

import android.arch.persistence.room.*
import android.os.Parcelable
import io.reactivex.Flowable
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "book")
data class Book(
        @PrimaryKey(autoGenerate = false)
        var url: String,
        var name: String,
        var author: String,
        var cover: String,
        var category: String,
        var intro: String,
        var srcs: String,
        var srcNames: String,
        var state: String = "",
        var updateTime: String = "",
        var updateChapter: String = "",
        var chapterUrl: String = "",
        var src: String = "",
        var srcName: String = "",
        var chapterCount: Int = 0,
        var chapterIndex: Int = 0,
        var chapterOffset: Long = 0L) : Parcelable {

    val authorExt
        get() = "作者：$author"

    val categoryExt
        get() = "类型：$category"

    val stateExt
        get() = "状态：$state"

    val updateTimeExt
        get() = "更新：$updateTime"

    val updateChapterExt
        get() = "章节：$updateChapter"

    val srcNameExt
        get() = "书源：$srcName"
}

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(books: List<Book>)

    @Query("SELECT * FROM book")
    fun listAll(): Flowable<List<Book>>

}