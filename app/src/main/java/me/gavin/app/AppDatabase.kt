package me.gavin.app

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import me.gavin.app.entity.Book
import me.gavin.app.entity.BookDao
import me.gavin.app.entity.Source
import me.gavin.app.entity.SourceDao


@Database(entities = [Source::class, Book::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sourceDao(): SourceDao

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) = Room
                .databaseBuilder(context.applicationContext, AppDatabase::class.java, "text.db")
                .fallbackToDestructiveMigration()
                // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()


        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER, " + "`name` TEXT, PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Book " + " ADD COLUMN pub_year INTEGER")
            }
        }
    }

}