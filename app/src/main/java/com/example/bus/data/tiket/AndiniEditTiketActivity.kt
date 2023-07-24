package com.example.bus.data.tiket

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bus.AndiniTiketActivity
import android.Manifest
import android.os.PersistableBundle
import com.example.bus.data.AndiniBusDatabase
import com.example.bus.databinding.ActivityEditTiketBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception

class AndiniEditTiketActivity : AppCompatActivity() {

    private var _binding: ActivityEditTiketBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var dataGambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""

    private var id_tiket: Int = 0

    lateinit var busDB: AndiniBusDatabase
    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(saveInstanceState: Bundle?) {
        super.onCreate(saveInstanceState)

        _binding = ActivityEditTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        busDB = AndiniBusDatabase(this@AndiniEditTiketActivity)

        val intent = intent
        binding.TxtNik.setText(intent.getStringExtra("nik_penumpang").toString())
        binding.TxtNama.setText(intent.getStringExtra("nama_penumpang").toString())
        binding.TxtNomor.setText(intent.getStringExtra("nomor_kursi").toString())
        binding.TxtKelasBus.setText(intent.getStringExtra("kelas_bus").toString())
        binding.TxtService.setText(intent.getStringExtra("service_tambahan").toString())

        id_tiket = intent.getStringExtra("id").toString().toInt()

        old_foto_dir = intent.getStringExtra("foto_ktp").toString()
        val imgFile = File("${Environment.getExternalStorageDirectory()}/${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.BtnImgKTP.setImageBitmap(myBitmap)

//        Log.e("foto", old_foto_dir)

        if (!checkPermission()) {
            requestPermissions()
        }

        binding.BtnImgKTP.setOnClickListener {
            openCamera()
        }

        binding.BtnEditTiket.setOnClickListener {
            editTiket()
        }
    }

    private fun checkPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        }
        else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED &&  read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try{
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                var uri = Uri.fromParts("package", this.packageManager.toString(), null)
                intent.data = uri
            }
            catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE)
        }
    }

    fun saveMediaToStorage(bitmap: Bitmap): String {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var image_save =""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            this.contentResolver?.also { resolver ->

                val contentValues = ContentValues().apply {
                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val ImageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                fos = ImageUri?.let { resolver.openOutputStream(it) }
                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        }
        else{

            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            }
            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imageDir, filename)
            fos = FileOutputStream(image)
            image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
        }
        fos?.use {

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK) {
            dataGambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(dataGambar!!)
            new_foto_dir = image_save_uri
            binding.BtnImgKTP.setImageBitmap(dataGambar)
        }
    }

    fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent,  REQ_CAM)
                }
            }
        }
    }

    private fun editTiket() {
        val nik_penumpang : Int = binding.TxtNik.text.toString().toInt()
        val nama_penumpang = binding.TxtNama.text.toString()
        val nomor_kursi : Int = binding.TxtNomor.text.toString().toInt()
        val kelas_bus = binding.TxtKelasBus.text.toString()
        val service_tambahan = binding.TxtService.text.toString()
        var foto_final_dir : String = old_foto_dir

        if (new_foto_dir != "") {
            foto_final_dir = new_foto_dir
            val imageDir =
                Environment.getExternalStoragePublicDirectory("")

            val old_foto_delete = File(imageDir, old_foto_dir)

            if (old_foto_delete.exists()) {

                if (old_foto_delete.delete()) {

                    Log.e("foto final", foto_final_dir)
                }
            }
        }
        lifecycleScope.launch {
            val tiket = AndiniTiket(nik_penumpang, nama_penumpang, nomor_kursi, kelas_bus, service_tambahan, foto_final_dir)
            tiket.id = id_tiket
            busDB.getAndiniTiketBus().updateTiket(tiket)
        }
        val intentTiket = Intent(this, AndiniTiketActivity::class.java)
        startActivity(intentTiket)
    }
}