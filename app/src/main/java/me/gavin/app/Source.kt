package me.gavin.app

import android.arch.persistence.room.*
import io.reactivex.Flowable

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/9/19.
 */
@Entity(tableName = "source")
class Source(
        @PrimaryKey(autoGenerate = false)
        val id: String,
        val name: String,
        val host: String,
        val ruleQueryUrl: String,
        val ruleQuerySelector: String,
        val ruleQueryName: String,
        val ruleQueryAuthor: String,
        val ruleQueryCover: String?,
        val ruleQueryIntro: String?,
        val ruleQueryHref: String,
        val ruleBookName: String?,
        val ruleBookAuthor: String?,
        val ruleBookCover: String?,
        val ruleBookCategory: String?,
        val ruleBookState: String?,
        val ruleBookLastTime: String?,
        val ruleBookLastChapter: String?,
        val ruleBookIntro: String?,
        val ruleBookHref: String,
        val ruleBookChapterSelector: String,
        val ruleChapterName: String,
        val ruleChapterHref: String,
        val ruleContent: String,
        val ruleContentFilter: String?,
        val remark: String?,
        var enable: Boolean)

@Dao
interface SourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sources: List<Source>)

    @Query("SELECT * FROM source")
    fun listAll(): Flowable<List<Source>>

}