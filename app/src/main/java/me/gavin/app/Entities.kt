package me.gavin.app

import android.arch.persistence.room.*
import android.content.Context
import io.reactivex.Flowable

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

@Database(entities = arrayOf(Source::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sourceDao(): SourceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        AppDatabase::class.java, "text.db")
                        .build()
    }

}

data class Book(val name: String, val author: String) {

    var cover: String? = null
    var intro: String? = null
    var srcNames: String = ""
}