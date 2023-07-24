package com.example.bus.data.tiket

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tiket")

data class AndiniTiket(
    @ColumnInfo(name = "nik_penumpang") var nik_penumpang: Int = 0,
    @ColumnInfo(name = "nama_penumpang") var nama_penumpang: String = "",
    @ColumnInfo(name = "nomor_kursi") var nomor_kursi: Int = 0,
    @ColumnInfo(name = "kelas_bus") var kelas_bus: String = "",
    @ColumnInfo(name = "service") var service: String = "",
    @ColumnInfo(name = "foto_tiket") var foto_tiket: String = "",
) :Serializable{
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}
