package com.example.bus

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bus.data.AndiniBusDatabase
import com.example.bus.data.tiket.AndiniTiket
import java.lang.Exception
import android.Manifest
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.example.bus.data.tiket.AndiniAddTiketFragment
import com.example.bus.databinding.ActivityTiketBinding
import kotlinx.coroutines.launch
import java.io.File

class AndiniTiketActivity : AppCompatActivity() {

    private var _binding: ActivityTiketBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION_TAG"

    lateinit var tiketRecyclerView: RecyclerView

    lateinit var busDB: AndiniBusDatabase

    lateinit var andinitiketList: ArrayList<AndiniTiket>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPermission()) {
            requestPermissions()
        }

        busDB = AndiniBusDatabase(this@AndiniTiketActivity)

        loadDataTiket()

        binding.btnAddTiket.setOnClickListener {
            AndiniAddTiketFragment().show(supportFragmentManager, "newTiketTag")
        }

        swipeDelete()

        binding.txtSearchTiket.addTextChangedListener {
            val keyword: String = "%${binding.txtSearchTiket.text.toString()}%"
            if (keyword.count() > 2) {
                searchDataTiket(keyword)
            }
            else {
                loadDataTiket()
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            }
            catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    private fun checkPermission() : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else{
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }


    fun loadDataTiket() {
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.tiketListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch{
            andinitiketList = busDB.getAndiniTiketBus().getAllAndiniTiket() as ArrayList<AndiniTiket>
            Log.e("List Tiket", andinitiketList.toString())
            tiketRecyclerView.adapter = AndiniTiketAdapter(andinitiketList)
        }
    }

    fun deleteTiket(tiket: AndiniTiket) {
        val builder = AlertDialog.Builder(this@AndiniTiketActivity)
        builder.setMessage("Apakah ${tiket.nama_penumpang} ingin dihapus ?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, id ->
                lifecycleScope.launch {
                    busDB.getAndiniTiketBus().deleteTiket(tiket)
                    loadDataTiket()
                }
                val imageDir =
                    Environment.getExternalStoragePublicDirectory("")
                val foto_delete = File(imageDir, tiket.foto_tiket)

                if (foto_delete.exists()) {
                    if (foto_delete.delete()) {
                        val toastDelete = Toast.makeText(applicationContext,
                        "file edit foto delete", Toast.LENGTH_LONG)
                        toastDelete.show()
                    }
                }
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.dismiss()
                loadDataTiket()
            }
        val alert = builder.create()
        alert.show()
    }

    fun swipeDelete() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0,
        ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                lifecycleScope.launch {
                    andinitiketList = busDB.getAndiniTiketBus().getAllAndiniTiket() as ArrayList<AndiniTiket>
                    Log.e("position swiped", andinitiketList[position].toString())
                    Log.e("position swiped", andinitiketList.size.toString())


                    deleteTiket(andinitiketList[position])
                }
            }
        }).attachToRecyclerView(tiketRecyclerView)
    }

    fun searchDataTiket(keyword: String){
        var layoutManager = LinearLayoutManager(this)
        tiketRecyclerView = binding.tiketListView
        tiketRecyclerView.layoutManager = layoutManager
        tiketRecyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            andinitiketList = busDB.getAndiniTiketBus().searchTiket(keyword) as ArrayList<AndiniTiket>
            Log.e("list tiket", andinitiketList.toString())
            tiketRecyclerView.adapter = AndiniTiketAdapter(andinitiketList)
        }
    }
}