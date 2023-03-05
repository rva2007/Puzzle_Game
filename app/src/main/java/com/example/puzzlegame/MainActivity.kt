package com.example.puzzlegame

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var imageFile:File? = null
    var mCurrentPhotoPath: String? = null
    var mUri: UriForIntent? = null
    private var requestCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )

        if (askForPermissions()) {
            // Permissions are already granted, do your stuff

            val assetManager = assets

            try {
                val files = assetManager.list("img")
                val gridView = findViewById<GridView>(R.id.gridView)

                gridView.adapter = ImageAdapter(this@MainActivity)
                gridView.onItemClickListener = AdapterView
                    .OnItemClickListener { adapterView, view, i, l ->

                        val intent = Intent(applicationContext, PuzzleActivity::class.java)
                        intent.putExtra("assetName", files!![i % files.size])
                        Log.d("MyLog", "in MainActivity " + files!![i % files.size])
                        startActivity(intent)
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isPermissionsAllowed(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED

        ) {
            false
        } else true
    }

    fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this as Activity,
                    Manifest.permission.CAMERA
                )
            ) {
                showPermissionDeniedDialog()
            }
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this as Activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this as Activity,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    ),
                    REQUEST_CODE
                )
            }


            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here


                } else {
                    // permission is denied, you can ask for permission again, if you want
                    askForPermissions()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    // send to app settings if permission is denied permanently
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                })
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun onImageCameraClicked(view: View) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this as Activity,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
        } else {
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
                if (photoFile != null) {
                    val photoUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        BuildConfig.APPLICATION_ID,
                        photoFile
                    )
                    mUri = UriForIntent(photoUri)
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, bitmap)

                }
            }
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
//        if (intent.resolveActivity(packageManager) != null) {
//            var photoFile: File? = null
//            try {
//                photoFile = createImageFile()
//            } catch (e: IOException) {
//                e.printStackTrace()
//                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
//            }
//            if (photoFile != null) {
//                val photoUri = FileProvider.getUriForFile(
//                    this@MainActivity,
//                    BuildConfig.APPLICATION_ID,
//                    photoFile
//                )
//                mUri = UriForIntent(photoUri)
////                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
//                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
//            }
//        }
//    else {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.CAMERA),
//                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE
//            )
//        }
//    }


    @Throws(IOException::class)
    private fun createImageFile(): File? {

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //permission  not granted initiate request
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE
            )
        } else {
            //create an image file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmsss").format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES
            )

            imageFile = File.createTempFile(
                imageFileName,
                "_jpeg",
                storageDir
            )
            //save this to use in the intent
            mCurrentPhotoPath = imageFile?.absolutePath
            imageFile?.deleteOnExit()
            return imageFile
        }
        return null
    }

    fun onImageGalleryClicked(view: View) {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQUEST_PERMISSION_READ_EXTERNAL_STORAGE
            )
        } else {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        this.requestCode = requestCode
        val intent = Intent(this@MainActivity, PuzzleActivity::class.java)

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val uri = data!!.data
            intent.putExtra("mCurrentPhotoPath", uri.toString())
            startActivity(intent)
        }
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            val uri = data!!.data
            intent.putExtra("mCurrentPhotoUri", uri.toString())
            startActivity(intent)
        }
    }


    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2

        const val REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 3

        const val REQUEST_IMAGE_GALLERY = 4
        const val REQUEST_CODE = 5
    }
}


