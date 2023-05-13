package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var currentColor: ImageButton? = null
    private var progressDialog: Dialog? = null

    /* This value helps us import the image, we pass it and launch it in the request
    permission intent
     */
    private val openFolderImageButton: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBk: ImageView = findViewById(R.id.import_image)

                imageBk.setImageURI(result.data?.data)
            }
        }

    /* Create an ActivityResultLauncher with MultiplePermissions to request permission,
    we need both read and write
     */
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            permission.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                //if the permission is granted, show a toast and perform action
                if (isGranted) {
                    val pickImage =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openFolderImageButton.launch(pickImage)
                    //otherwise, perform another toast saying read external storage is denied
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this, "Oops, you just denied permission", Toast.LENGTH_SHORT)
                            .show()
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
        /*set the first color to be current color drawable, for this we need to find by id that
        linear layout
         */
        val linearLayout = findViewById<LinearLayout>(R.id.linear_layout_colors)

        currentColor = linearLayout[0] as ImageButton
        currentColor!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.selected_color_palette)
        )

        val brushPaintIcon = findViewById<ImageButton>(R.id.brushSize)
        brushPaintIcon.setOnClickListener {
            showDialogBrushSize()
        }

        val imageButton: ImageButton = findViewById(R.id.folder_image_button)
        imageButton.setOnClickListener {
            requestStoragePermission()
        }

        val undoBtn: ImageButton = findViewById(R.id.undo_button)
        undoBtn.setOnClickListener {
            drawingView?.onClickUndo()
        }

        val redoBtn: ImageButton = findViewById(R.id.redo_button)
        redoBtn.setOnClickListener {
            drawingView?.onClickRedo()
        }

        val saveBtn: ImageButton = findViewById(R.id.save)
        saveBtn.setOnClickListener {
            if (isReadStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch {
                    val frameLayout: FrameLayout = findViewById(R.id.frame_layout)
                    saveBitmap(getBitmapFromView(frameLayout))
                }
            }
        }
    }

    // This function is used to show dialog brush size view with each brush size
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

    /* This function is for checking if we have selected color. Function is called in
    'onClick' in the main layout
     */
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
    private fun showRationaleDialog(title: String, message: String) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    // Create a function to request storage permission
    private fun requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        ) {
            showRationaleDialog("Kids drawing app", "Drawing app needs your permission")
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // This function is used to actually create the Bitmap from View and return it
    private fun getBitmapFromView(view: View): Bitmap {
        //define bitmap with the same size as View
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //bind a canvas to it
        val canvas = Canvas(bitmap)
        //get view's background
        val background = view.background
        if (background != null) {
            //if it has bg drawable, draws it on the canvas
            background.draw(canvas)
        } else {
            //if not, draws white color
            canvas.drawColor(Color.WHITE)
        }
        //draw view on the canvas
        view.draw(canvas)

        return bitmap
    }

    // This function is used to save our image using coroutines and suspending functions
    private suspend fun saveBitmap(bitmap: Bitmap): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    val byteStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, byteStream)

                    val f = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "DrawingApp" + System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fileOutput = FileOutputStream(f)
                    fileOutput.write(byteStream.toByteArray())
                    fileOutput.close()

                    result = f.absolutePath

                    runOnUiThread {
                        cancelProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                applicationContext,
                                "File saved successfully: $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            shareSavedImage(result)
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Something went wrong!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    /* This function is used to check permission status. We need to call this function in the 'if'
    block. If true, it starts the 'lifecycleScope' block, with other functions that save the image
     */
    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    // This function is used to run progress bar layout on UI thread
    private fun showProgressDialog() {
        progressDialog = Dialog(this)
        progressDialog?.setContentView(R.layout.progress_dialog)
        progressDialog?.show()
    }

    // This function is used to cancel that showProgressDialog() function
    private fun cancelProgressDialog() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    // This function is used to prompt chooser for our saved image to share with other apps
    private fun shareSavedImage(result: String) {
        /*MediaScannerConnection provides a way for applications to pass a
        newly created or downloaded media file to the media scanner service.
        The media scanner service will read metadata from the file and add
        the file to the media content provider.
        The MediaScannerConnectionClient provides an interface for the
        media scanner service to return the Uri for a newly scanned file
        to the client of the MediaScannerConnection class.*/
        MediaScannerConnection.scanFile(this, arrayOf(result), null) { path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(
                Intent.createChooser(
                    shareIntent, "Share"
                )
            )
        }
    }
}