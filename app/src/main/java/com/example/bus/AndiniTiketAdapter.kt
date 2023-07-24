package com.example.bus

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.bus.data.tiket.AndiniEditTiketActivity
import com.example.bus.data.tiket.AndiniTiket
import java.io.File

class AndiniTiketAdapter (private val AndiniTiketList: ArrayList<AndiniTiket>) :
RecyclerView.Adapter<AndiniTiketAdapter.TiketViewHolder>() {

    private lateinit var activity: AppCompatActivity

    class TiketViewHolder (tiketItemView : View) : RecyclerView.ViewHolder(tiketItemView) {

        val nama_penumpang: TextView = tiketItemView.findViewById(R.id.TVLNamaPenumpang)
        val nomor_kursi: TextView = tiketItemView.findViewById(R.id.TVLNomorKursi)
        val kelas_bus: TextView = tiketItemView.findViewById(R.id.TVLKelasBus)

        val img_tiket: ImageView = itemView.findViewById(R.id.IMLGambarTiket)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiketViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tiket_list_layout, parent, false)
        return TiketViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TiketViewHolder, position: Int) {
        val currentItem = AndiniTiketList[position]
        val foto_dir = currentItem.foto_tiket.toString()
        val ImgFile = File("${Environment.getExternalStorageDirectory()}/${foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(ImgFile.absolutePath)

        holder.img_tiket.setImageBitmap(myBitmap)
        holder.nama_penumpang.text =currentItem.nama_penumpang.toString()
        holder.nomor_kursi.text =currentItem.nomor_kursi.toString()
        holder.kelas_bus.text =currentItem.kelas_bus.toString()

        holder.itemView.setOnClickListener {
        Log.e("Adapter_Click", "clicked")

            activity = it.context as AppCompatActivity
            activity.startActivity(Intent(activity, AndiniEditTiketActivity::class.java).apply {
                putExtra("nik_penumpang", currentItem.nik_penumpang.toString())
                putExtra("nama_penumpang", currentItem.nama_penumpang.toString())
                putExtra("nomor_kursi", currentItem.nomor_kursi.toString())
                putExtra("kelas_bus", currentItem.kelas_bus.toString())
                putExtra("servive_tambahan", currentItem.service.toString())
                putExtra("foto_ktp", currentItem.foto_tiket.toString())
                putExtra("id", currentItem.id.toString())
            })
        }
    }

    override fun getItemCount(): Int {
        return AndiniTiketList.size
    }

}