package com.epicorebiosystems.rehydrate.fileHandler

import android.content.Context
import com.epicorebiosystems.rehydrate.modelData.ModelData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileManager(private val chViewModel: ModelData) {

    fun deleteFile(filePath: String): Boolean {
        val path = chViewModel.applicationContext!!.filesDir
        val file = File(path, filePath)

        if (!file.exists()) return false

        return file.deleteRecursively()
    }

    fun writeFile(filePath: String, csvText: String): Boolean {
        //Log.d("WRITE_FILE", "$filePath")
        return try {
            val fos: FileOutputStream = chViewModel.applicationContext!!.openFileOutput(filePath, Context.MODE_PRIVATE)
            fos.write(csvText.toByteArray())
            fos.flush()
            fos.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun readFile(filePath: String): String? {
        return try {
            // Open the same private file you wrote earlier
            val fis = chViewModel.applicationContext!!
                .openFileInput(filePath)
            // Read its entire contents as a String
            fis.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun fileExists(filePath: String): Boolean {
        val path = chViewModel.applicationContext!!.filesDir
        val file = File(path, filePath)
        return file.exists()
    }

}
