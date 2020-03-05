package wgnelther.faces

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.opencv.wgnelther.faces.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // get app data folder
        val mPath = applicationInfo.dataDir + "/.rec"
        Log.d("AAA", mPath)

        // Save the image in internal storage and get the uri
        createDirectoryAndSaveFile(R.drawable.unknown_0, "Unknown-0.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_1, "Unknown-1.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_2, "Unknown-2.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_3, "Unknown-3.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_4, "Unknown-4.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_5, "Unknown-5.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_6, "Unknown-6.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_7, "Unknown-7.jpg", mPath)
        createDirectoryAndSaveFile(R.drawable.unknown_8, "Unknown-8.jpg", mPath)

        // create .nomedia
        val file = File(mPath, ".nomedia")
        try {
            file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // write label 0
        File(mPath, "faces.txt").printWriter().use { out ->
            out.println("Unknown,1")
        }

        // auto next intent
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }

    private fun createDirectoryAndSaveFile(drawableId: Int, fileName: String, mPath: String) {

        val drawable = ContextCompat.getDrawable(applicationContext, drawableId)
        val bitmap = (drawable as BitmapDrawable).bitmap
        val bitmapScaled = Bitmap.createScaledBitmap(bitmap, 128, 128, false)

        File(mPath).mkdirs()
        val file = File(mPath, fileName)
        if (file.exists()) {
            return
        }

        try {
            val out = FileOutputStream(file)
            bitmapScaled.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
