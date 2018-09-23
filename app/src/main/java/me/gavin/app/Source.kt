package me.gavin.app

import android.arch.persistence.room.*
import io.reactivex.Flowable


const val SOURCE_FLAG_NONE = 0 // 正常状态
const val SOURCE_FLAG_DISABLE = 1 // 不可用
const val SOURCE_FLAG_CHECKED = 1 shl 1 // 选中
const val SOURCE_FLAG_TOP = 1 shl 2 // 置顶
const val SOURCE_FLAG_SYS = 1 shl 3 // 内置

@Entity(tableName = "source")
data class Source(
        @PrimaryKey(autoGenerate = false)
        val url: String,
        val name: String,
        val ruleQueryUrl: String,
        val ruleQueryList: String,
        val ruleQueryName: String,
        val ruleQueryAuthor: String,
        val ruleQueryCover: String?,
        val ruleQueryCategory: String?,
        val ruleQueryIntro: String?,
        val ruleQueryBookUrl: String,
        val ruleBookName: String?,
        val ruleBookAuthor: String?,
        val ruleBookCover: String?,
        val ruleBookCategory: String?,
        val ruleBookState: String?,
        val ruleBookLastTime: String?,
        val ruleBookLastChapter: String?,
        val ruleBookIntro: String?,
        val ruleBookChapterUrl: String,
        val ruleChapterList: String,
        val ruleChapterName: String,
        val ruleChapterContentUrl: String,
        val ruleContent: String,
        val ruleContentFilter: String?,
        val ruleDiscoveryUrl: String?,
        val remark: String?,
        val userAgent: String?,
        val soft: Int = 100,
        var flag: Int = SOURCE_FLAG_NONE)

@Dao
interface SourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(sources: List<Source>)

    @Query("SELECT * FROM source")
    fun listAll(): Flowable<List<Source>>

}