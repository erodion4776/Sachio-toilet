package com.sashio.ops.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfile::class,
        Booking::class,
        Payment::class,
        Consultation::class,
        Review::class,
        CatalogItem::class,
        FavoriteLocation::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SashioDatabase : RoomDatabase() {

    abstract fun sashioDao(): SashioDao

    companion object {
        @Volatile
        private var INSTANCE: SashioDatabase? = null

        fun getDatabase(context: Context): SashioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SashioDatabase::class.java,
                    "sashio_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
