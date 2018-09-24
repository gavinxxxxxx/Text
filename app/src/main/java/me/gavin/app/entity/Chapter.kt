package me.gavin.app.entity

import android.arch.persistence.room.*
import io.reactivex.Flowable


@Entity(tableName = "chapter")
data class Chapter(
        @PrimaryKey(autoGenerate = false)
        val url: String,
        val title: String,
        val bookUrl: String,
        val index: Int,
        val offset: Long = 0,
        val selected: Boolean = false,
        var text: String = "")

@Dao
interface ChapterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(chapters: List<Chapter>)

    @Query("SELECT * FROM chapter WHERE bookUrl = :bookUrl")
    fun listAll(bookUrl: String): Flowable<List<Chapter>>

}