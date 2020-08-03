package com.liao.camera.activities

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.liao.camera.R
import com.liao.camera.adapters.AdapterRecyclerView
import com.liao.camera.helper.toolbar
import com.liao.camera.model.PhotoInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private var mlist: ArrayList<PhotoInfo> = ArrayList()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val CAMERA_REQUEST_CODE = 101
    private val CHOICE_FROM_ALBUM_REQUEST_CODE = 102
    lateinit var adapterRecyclerView: AdapterRecyclerView
//    private val rootPath: String = android.os.Environment.getExternalStorageDirectory().absolutePath
//    private var imagePath = File("$rootPath/Camera/", System.currentTimeMillis().toString() + ".jpg")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar("My Camera")

        init()
    }

    private fun init() {

        button_floating.setOnClickListener {
            slideUpBottomSheet()
            //showBottomSheetDialog()
        }
        item_camera.setOnClickListener {
            onMultiplePermission()
        }
        item_gallery.setOnClickListener {
            onSinglePermission()
        }
        bottomSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottomSheet)

        recycler_view.layoutManager = GridLayoutManager(this, 3)
        adapterRecyclerView =
            AdapterRecyclerView(this, mlist)
        recycler_view.adapter = adapterRecyclerView


        val rv = findViewById<RecyclerView>(R.id.recycler_view)
        val behavior = BottomSheetBehavior.from(findViewById<LinearLayout>(R.id.bottomSheet))
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback(){
            override fun onSlide(bottomSheet: View, offset: Float) {
                rv.setPadding(0, 0, 0, (bottomSheet.height * offset).toInt())
            }

            override fun onStateChanged(bottomSheet: View, newState: Int){}
        })



    }

    private fun onSinglePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    choiceFromAlbum()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if(response!!.isPermanentlyDenied){
                        Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT)
                            .show()
                        showMyDialog()
                    }
                }
            }).check()
    }

    private fun onMultiplePermission() {
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    Toast.makeText(
                        applicationContext,
                        "All permission granted",
                        Toast.LENGTH_SHORT
                    ).show()
                    openCamera()
                }

                if (report.isAnyPermissionPermanentlyDenied) {
                    Toast.makeText(
                        applicationContext,
                        "permission denied permanently",
                        Toast.LENGTH_SHORT
                    ).show()
                    showMyDialog()
                }
                if(report.deniedPermissionResponses.isNotEmpty()){
                    val listOfDeniedResponse = report.deniedPermissionResponses
                    var flag = false
                    for(item in listOfDeniedResponse){
                        if(item.permissionName == "android.permission.CAMERA"){
                            flag = true
                        }
                    }
                    if(!flag){
                        openCamera()
                    }
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                permission: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token!!.continuePermissionRequest()

            }
        }).onSameThread().check()
    }

    private fun showMyDialog() {
        var builder = AlertDialog.Builder(this)
        builder.setTitle("Need U Permission")
        builder.setMessage("please give permission")
        builder.setPositiveButton("Go to Settings", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                dialog?.dismiss()
                openSettings()
            }
        })

        builder.setNegativeButton("Cancel")
        { dialog, p1 -> dialog.dismiss() }
        builder.show()
    }

    private fun openSettings() {
        var myIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        var uri = Uri.fromParts("package", packageName, null)
        myIntent.data = uri
        startActivityForResult(myIntent, CAMERA_REQUEST_CODE)
    }

    private fun choiceFromAlbum() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT).setType("image/*"),
                CHOICE_FROM_ALBUM_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, CHOICE_FROM_ALBUM_REQUEST_CODE)
        }

    }


    private fun openCamera() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (!imagePath.exists()) {
//            imagePath.parentFile.mkdir();
//        }
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagePath))
//        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            var photo: Bitmap = data?.extras?.get("data") as Bitmap
            val name = System.currentTimeMillis().toString() + ".jpg"
            var photoInfo = PhotoInfo(photo,name)
            mlist.add(photoInfo)
            MediaStore.Images.Media.insertImage(contentResolver,photo, name, "description")
        }
        if (requestCode == CHOICE_FROM_ALBUM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val uri = data.data
                val uriString = uri.toString()
                val myFile = File(uriString)
                var displayName: String? = null
                if (uriString.startsWith("content://")) {
                    var cursor: Cursor? = null
                    try {
                        cursor =
                            contentResolver.query(uri!!, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName =
                                cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                        }
                    } finally {
                        cursor?.close()
                    }

                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.name;
                }

                var photo: Bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                var photoInfo = PhotoInfo(photo, displayName!!)
                mlist.add(photoInfo)
                adapterRecyclerView.setList(mlist)
            } else {
                Toast.makeText(this, "please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun slideUpBottomSheet() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            button_floating.setImageResource(R.drawable.ic_baseline_remove_24)
            recycler_view.minimumHeight = recycler_view.height - bottomSheet.height
        } else {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
            button_floating.setImageResource(R.drawable.ic_baseline_photo_camera_24)
        }
    }

//    private fun showBottomSheetDialog() {
//       var bottomSheetFragment = BottomSheetFragment()
//        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag);
//    }

}