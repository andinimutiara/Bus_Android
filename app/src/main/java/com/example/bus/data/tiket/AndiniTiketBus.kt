package com.example.bus.data.tiket

import androidx.room.*

@Dao
interface AndiniTiketBus {
    @Query("SELECT * FROM tiket WHERE nama_penumpang LIKE :namaPenumpang")
    suspend fun searchTiket(namaPenumpang: String) : List<AndiniTiket>

    @Insert
    suspend fun addTiket(andiniTiket: AndiniTiket)

    @Update
    suspend fun updateTiket(andiniTiket: AndiniTiket)

    @Delete
    suspend fun deleteTiket(andiniTiket: AndiniTiket)

    @Query("SELECT * FROM tiket")
    suspend fun getAllAndiniTiket() : List<AndiniTiket>
}