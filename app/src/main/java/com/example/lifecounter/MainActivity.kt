package com.example.lifecounter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.super_rabbit.wheel_picker.WheelPicker
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var imageSelected: ImageView
    lateinit var wheelPicker1: WheelPicker
    lateinit var wheelPicker2: WheelPicker

    var count by Delegates.notNull<Int>()

    private val REQUEST_CODE_STORAGE_PERMISSION: Int = 1
    private val REQUEST_CODE_SELECT_IMAGE = 2
    private val STARTING_LIFE = 40

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.sharedPreferences = getSharedPreferences("count", Context.MODE_PRIVATE)
        this.editor = sharedPreferences.edit()
        this.imageSelected = findViewById(R.id.imageView)
        this.wheelPicker1 = findViewById(R.id.wheel1)
        this.wheelPicker2 = findViewById(R.id.wheel2)

        loadLife()
        loadImagePref()
        wheelProperties()

    }

    private fun wheelProperties() {
        wheelPicker1.setMin(0)
        wheelPicker1.setMax(10)
        wheelPicker1.setSelectedTextColor(R.color.colorAccent)
        wheelPicker1.visibility = View.INVISIBLE

        wheelPicker2.setMin(0)
        wheelPicker2.setSelectedTextColor(R.color.colorAccent)
        wheelPicker2.visibility = View.INVISIBLE
    }

    private fun loadLife() {
        sharedPreferences = getSharedPreferences("count", Context.MODE_PRIVATE)
        count = sharedPreferences.getInt("count", 0)
        setLife()
    }

    private fun loadImagePref() {
        var imageS: String? = sharedPreferences.getString("imagePreference", "")

        if (!imageS.equals("")) {
            var imageB: Bitmap? = imageS?.let { decodeToBase64(it) }
            imageSelected.setImageBitmap(imageB)
        }
    }

    fun decodeToBase64(input: String): Bitmap? {
        var decodedByte = Base64.decode(input, 0)
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
    }

    private fun commitToShared() {
        editor.putInt("count", count)
        editor.commit()
    }

    private fun setLife() {
        val textview = findViewById<TextView>(R.id.life)
        textview.text = "$count"
    }

    fun increaseOnTap(view: View) {
        if (count != STARTING_LIFE) {
            count = sharedPreferences.getInt("count", 0)
            count++
            setLife()
        } else {
            count++
            setLife()
        }
        commitToShared()
    }

    fun decreaseOnTap(view: View) {
        if (count != STARTING_LIFE) {
            count = sharedPreferences.getInt("count", 0)
            count--
            setLife()
        } else {
            count--
            setLife()
        }
        commitToShared()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> {
                count = STARTING_LIFE
                setLife()
                commitToShared()
                return true
            }
            R.id.item2 -> {
                changeImage()
                return true
            }
            R.id.item3 -> {
                if(item.isChecked) {
                    item.isChecked = false
                    var parms: ConstraintLayout.LayoutParams =
                        ConstraintLayout.LayoutParams(dpToPx(400), dpToPx(724))
                    imageSelected.layoutParams = parms
                    return true
                } else {
                    item.isChecked = true
                    var parms: ConstraintLayout.LayoutParams =
                        ConstraintLayout.LayoutParams(dpToPx(400), dpToPx(380))
                    imageSelected.layoutParams = parms
                    return true
                }
            }
            R.id.item4 -> {
                if(item.isChecked) {
                    item.isChecked = false
                    wheelPicker1.visibility = View.INVISIBLE
                    return true
                } else {
                    item.isChecked = true
                    wheelPicker1.visibility = View.VISIBLE
                    return true
                }
            }
            R.id.item5 -> {
                if(item.isChecked) {
                    item.isChecked = false
                    wheelPicker2.visibility = View.INVISIBLE
                    return true
                } else {
                    item.isChecked = true
                    wheelPicker2.visibility = View.VISIBLE
                    return true
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
    }

    private fun changeImage() {
        if (ContextCompat.checkSelfPermission(
                applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        } else {
            selectImage()
        }
    }

    private fun selectImage(){
        var intent: Intent
                = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

    }

    @SuppressLint("ShowToast")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty()) {
            selectImage()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                var selectedImageUri: Uri? = data.data

                if (selectedImageUri != null) {
                    try {

                        var inputStream: InputStream?
                                = contentResolver.openInputStream(selectedImageUri)
                        var bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        imageSelected.setImageBitmap(bitmap)

                        editor.putString("imagePreference", encodeToBase64(bitmap))
                        editor.commit()

                    } catch (exception: Exception) {
                        Toast.makeText(this, exception.message, Toast.LENGTH_SHORT)
                    }
                }
            }
        }
    }

    private fun encodeToBase64(image: Bitmap): String {
        var bitmap: Bitmap = image
        var baos: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        var b = baos.toByteArray()
        var imageEncoded: String = Base64.encodeToString(b, Base64.DEFAULT)

        Log.d("Image Log:", imageEncoded)
        return imageEncoded
    }
}