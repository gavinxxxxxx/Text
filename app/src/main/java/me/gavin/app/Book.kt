package me.gavin.app

import android.arch.persistence.room.*
import io.reactivex.Flowable
import java.io.Serializable


// todo @Parcelize
@Entity(tableName = "book")
data class Book(
        @PrimaryKey(autoGenerate = false)
        var url: String,
        val name: String,
        val author: String,
        var cover: String,
        var category: String,
        var intro: String,
        var srcNames: String) : Serializable {
}

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(books: List<Book>)

    @Query("SELECT * FROM book")
    fun listAll(): Flowable<List<Book>>

}