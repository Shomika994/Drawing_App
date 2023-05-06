package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var currentColor: ImageButton? = null


    val openFolderImageButton: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK && result.data != null){
                val imageBk: ImageView = findViewById(R.id.import_image)

                imageBk.setImageURI(result.data?.data)
            }
        }
    //create an ActivityResultLauncer with MultplePermissions
    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permission ->
            permission.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted){
                    Toast.makeText(this, "Permission granted now you can read the storage files", Toast.LENGTH_SHORT).show()
                    val pickImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openFolderImageButton.launch(pickImage)

                } else {
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this, "Oops, you just denied permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        drawingView = findViewById(R.id.drawingView)
        drawingView?.setBrushSize(20.toFloat())

        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_colors)

        currentColor = linearLayout[1] as ImageButton
        currentColor!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.selected_color_palette)
        )

        val brushPaintIcon = findViewById<ImageButton>(R.id.brushSize)
        brushPaintIcon.setOnClickListener {
            showDialogBrushSize()
        }

        val imageButton: ImageButton = findViewById(R.id.folder_image_button)
        imageButton.setOnClickListener{
            requestStoragePermission()
        }

    }


    private fun showDialogBrushSize() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.image_small)
        smallBtn.setOnClickListener {
            drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.image_medium)
        mediumBtn.setOnClickListener {
            drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.image_large)
        largeBtn.setOnClickListener {
            drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun colorClicked(view: View) {
        if (view !== currentColor) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.selected_color_palette)
            )
            currentColor?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_palette)
            )
            currentColor = view
        }
    }

    /** Todo 8: create rationale dialog
     * Shows rationale dialog for displaying why the app needs permission
     * Only shown if the user has denied the permission request previously
     */
    private fun showRationaleDialog(title: String, message: String){

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){dialog, _-> dialog.dismiss()}
        builder.create().show()
    }

    //create a function to request storage permission
    private fun requestStoragePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationaleDialog("Kids drawing app", "Drawing app needs your permission")
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        }
    }
}