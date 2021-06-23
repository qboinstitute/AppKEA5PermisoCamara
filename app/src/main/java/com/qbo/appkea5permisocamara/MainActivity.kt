package com.qbo.appkea5permisocamara

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.qbo.appkea5permisocamara.commom.Constantes
import com.qbo.appkea5permisocamara.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var rutaFotoActual = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btntomarfoto.setOnClickListener {
            if(validarPermisoEscritura()){
                try {
                    invocarAppCamara()
                }catch (e: IOException){

                }
            }else{
                solicitarPermisoEscritura()
            }
        }
    }
    private fun validarPermisoEscritura() : Boolean{
        val permiso = ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permiso == PackageManager.PERMISSION_GRANTED
    }
    private fun solicitarPermisoEscritura(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            Constantes.ID_REQUEST_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == Constantes.ID_REQUEST_PERMISSION){
            if(grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                invocarAppCamara()
            }else{
                Toast.makeText(applicationContext,
                    "Es necesario contar con el permiso para tomar fotos",
                    Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun crearArchivoTemporal() : File{
        val nombreImagen = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss")
            .format(Date())
        val directorioImagenes: File =
            this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val archivoTemporal: File = File.createTempFile(nombreImagen,
            ".jpg", directorioImagenes)
        rutaFotoActual = archivoTemporal.absolutePath
        return archivoTemporal
    }
    private fun obtenerContentUri(archivo: File) : Uri{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            FileProvider.getUriForFile(
                applicationContext,
                "com.qbo.appkea5permisocamara.fileprovider",
                archivo
            )
        } else{
            Uri.fromFile(archivo)
        }
    }

    @Throws(IOException::class)
    private fun invocarAppCamara(){
        val intentCamara = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intentCamara.resolveActivity(packageManager) != null){
            val archivoFoto = crearArchivoTemporal()
            if(archivoFoto != null){
                val fotoUri = obtenerContentUri(archivoFoto)
                intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
                startActivityForResult(intentCamara, Constantes.ID_CAMARA_REQUEST)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == Constantes.ID_CAMARA_REQUEST){
            if(resultCode == Activity.RESULT_OK){
                mostrarFoto()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun mostrarFoto(){
        val anchoIv = binding.ivfoto.width
        val altoIv = binding.ivfoto.height
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(rutaFotoActual, bmOptions)
        val anchoFoto = bmOptions.outWidth
        val altoFoto = bmOptions.outHeight
        val escalaFoto = min(anchoFoto / anchoIv , altoFoto / altoIv)
        bmOptions.inSampleSize = escalaFoto
        bmOptions.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(rutaFotoActual, bmOptions)
        binding.ivfoto.setImageBitmap(bitmap)
    }
}