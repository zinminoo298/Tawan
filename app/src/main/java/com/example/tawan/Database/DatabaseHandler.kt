package com.example.tawan.Database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import com.example.tawan.edt_bc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

class DatabaseHandler(val context: Context) {

    val REAL_DATABASE="database.db"
    var barcode = ""
    var description = ""
    var weight = ""
    var date = ""
    var number = ""


    fun openDatabase(): SQLiteDatabase {
        val dbFile=context.getDatabasePath(REAL_DATABASE)
        if (!dbFile.exists()) {
            try {
                val checkDB=context.openOrCreateDatabase(REAL_DATABASE, Context.MODE_PRIVATE, null)

                checkDB?.close()
                copyDatabase(dbFile)
            } catch (e: IOException) {
                throw RuntimeException("Error creating source database", e)
            }

        }
        return SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
    }

    private fun copyDatabase(dbFile: File) {
        val `is`=context.assets.open(REAL_DATABASE)
        val os= FileOutputStream(dbFile)

        val buffer=ByteArray(1024)
        while(`is`.read(buffer)>0) {
            os.write(buffer)
            Log.d("#DB", "writing>>")
        }

        os.flush()
        os.close()
        `is`.close()
        Log.d("#DB", "completed..")
    }

    fun getDetail(){
        try {
            val db = context.openOrCreateDatabase(REAL_DATABASE, Context.MODE_PRIVATE, null)
            val query = "SELECT * FROM  master WHERE barcode ='$edt_bc'"
            val cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                    barcode = cursor.getString(0)
                    description = cursor.getString(1)
                    weight = cursor.getString(2)
                    date = cursor.getString(3)
                    number = cursor.getString(4)

                    println("YES")
            }

            else{
                Toast.makeText(context,"Barcode not found",Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            println("NOPE  " + e)
        }
    }

}