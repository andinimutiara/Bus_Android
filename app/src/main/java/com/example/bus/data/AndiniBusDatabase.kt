package com.example.bus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bus.data.tiket.AndiniTiket
import com.example.bus.data.tiket.AndiniTiketBus
import java.util.concurrent.locks.Lock


@Database(entities = [AndiniTiket::class], version = 1)
abstract class AndiniBusDatabase : RoomDatabase() {

    abstract fun getAndiniTiketBus(): AndiniTiketBus

    companion object {
        @Volatile
        private var instance: AndiniBusDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(Lock) {
            instance ?: buildDatabase(context).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =  Room.databaseBuilder(
            context.applicationContext,
            AndiniBusDatabase::class.java,
            "bus-db"
        ).build()
    }
}