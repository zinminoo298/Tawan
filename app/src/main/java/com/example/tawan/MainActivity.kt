package com.example.tawan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tawan.Database.DatabaseHandler
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

var file:String? =null
var noti: String?="Select Master File"
var line:Int? = 0
var com_check:String? = null
var in_line:Int? = 0
var code_check:String? =null
var edt_bc:String = ""

class MainActivity : AppCompatActivity() {

    internal lateinit var db:DatabaseHandler
    internal lateinit var barcode:EditText
    internal lateinit var btnOk:Button
    internal lateinit var lbl: TextView
    private var progressBar: ProgressBar?=null
    internal lateinit var cond:TextView
    internal lateinit var btnimport: EditText
    internal lateinit var scan: Button
    internal lateinit var exit:Button
    var MB:String? = null
    var STORAGE_PERMISSION_CODE = 1;
    val requestcode = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHandler(this)
        db.openDatabase()

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage Permission is Granted",
                Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }


        edt_barcode.setOnKeyListener { v, keyCode, event ->
            if (event.keyCode == KeyEvent.KEYCODE_SPACE && event.action == KeyEvent.ACTION_UP || event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if(edt_barcode.text.toString() == ""){
                    Toast.makeText(this,"Please Enter Barcode",Toast.LENGTH_SHORT).show()
                }
                else{
                    edt_bc = edt_barcode.text.toString()
                    db.getDetail()
                    txt_barcode.setText(db.barcode)
                    txt_desc.setText(db.description)
                    txt_weight.setText(db.weight)
                    txt_date.setText(db.date)
                    txt_number.setText(db.number)
                    btn_ok.isFocusable = false
                    edt_barcode.text.clear()
                    edt_barcode.requestFocus()
                }
            }
            false
        }

        btnOk = findViewById(R.id.btn_ok)
        btnOk.setOnClickListener {

            if(edt_barcode.text.toString() == ""){
                Toast.makeText(this,"Please Enter Barcode",Toast.LENGTH_SHORT).show()
            }
            else{
                edt_bc = edt_barcode.text.toString()
                db.getDetail()
                txt_barcode.setText(db.barcode)
                txt_desc.setText(db.description)
                txt_weight.setText(db.weight)
                txt_date.setText(db.date)
                txt_number.setText(db.number)
                edt_barcode.text.clear()
            }
//            importDialog(this)
        }
    }

    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE
            )
        }

        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun importDialog(context: Context) {
        val builder= AlertDialog.Builder(this)
        val inflater=this.layoutInflater
        val view=inflater.inflate(R.layout.import_dialog, null)
        builder.setView(view)
        val dialog: AlertDialog =builder.create()
//        dialog.window?.attributes?.windowAnimations=type
        dialog.setMessage("Please Open Master File")
        lbl=EditText(this)
        lbl=view.findViewById(R.id.edit_master)
        lbl.text=noti.toString()
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE
        cond = view.findViewById(R.id.in_cond)
        cond.setText("Importing ...")
        cond!!.visibility = View.GONE
        dialog.show()

        db= DatabaseHandler(this)
        val async = AsyncTaskRunner(this)


        btnimport=view.findViewById(R.id.edit_master)

        btnimport.setOnClickListener {
            cond.visibility = View.GONE
            val fileintent=Intent(Intent.ACTION_GET_CONTENT)
//            fileintent.addCategory(Intent.CATEGORY_OPENABLE)
            fileintent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            fileintent.type = "*/*"
            try {
                startActivityForResult(fileintent, requestcode)
                dialog.show()
            } catch (e: ActivityNotFoundException) {
                lbl.text="No activity can handle picking a file. Showing alternatives."
            }
        }

        scan=view.findViewById<Button>(R.id.btn_scan)
        scan.setOnClickListener {
            //            progressBar!!.visibility = View.VISIBLE
            cond.setText("Importing ...")
            cond!!.visibility = View.VISIBLE
            scan.setEnabled(false)
            exit.setEnabled(false)
            async.execute()
            dialog.dismiss()

//            Handler().postDelayed({
//                import()
//
//            },2000)

        }

        exit = view.findViewById<Button>(R.id.btn_cancle)
        exit.setOnClickListener {
            dialog.dismiss()
//            val intent=intent
//            finish()
//            startActivity(intent)

        }


    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null)
            return
        if (requestCode==requestcode) {
            val filepath=data.data
            file = filepath.toString()
            val fileee = File(file!!)
            val size = fileee.length()
            val sizeMB = size / 1024
            MB = sizeMB.toString()
            println("SIZE IS "+MB)
            val cursor=contentResolver.openInputStream(android.net.Uri.parse(filepath.toString()))
            lbl.text=filepath.toString()
//            master_path=filepath.toString()
            noti=cursor.toString()
            val db=this.openOrCreateDatabase("database.db", Context.MODE_PRIVATE, null)
            val tableName="Master"
//            db.execSQL("delete from $tableName")
            val text =  StringBuilder()
            try {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val file=InputStreamReader(cursor)
                        val buffer=BufferedReader(file)
                        var lineCount = 0
                        db.beginTransaction()

                        while(true) {
                            val line1=buffer.readLine()
                            lineCount++
                            line=lineCount
                            if (line1 == null) break


                        }
                        println(line.toString())
//                        line = lineCount.toString().toInt()
                        db.setTransactionSuccessful()
                        db.endTransaction()
                    } catch (e: IOException) {
                        if (db.inTransaction())
                            db.endTransaction()
                        val d=Dialog(this)
                        d.setTitle(e.message.toString() + "first")
                        d.show()
                    }

                } else {
                    if (db.inTransaction())
                        db.endTransaction()
                    val d=Dialog(this)
                    d.setTitle("Only CSV files allowed")
                    d.show()
                }
            } catch (ex: Exception) {
                if (db.inTransaction())
                    db.endTransaction()

                val d=Dialog(this)
                d.setTitle(ex.message.toString() + "second")
                d.show()
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.action_one) {
            Toast.makeText(this, "Item One Clicked", Toast.LENGTH_LONG).show()
            importDialog(this)
            return true
        }
//        if (id == R.id.action_two) {
//            Toast.makeText(this, "Item Two Clicked", Toast.LENGTH_LONG).show()
//            return true
//        }
//        if (id == R.id.action_three) {
//            Toast.makeText(this, "Item Three Clicked", Toast.LENGTH_LONG).show()
//            return true
//        }

        return super.onOptionsItemSelected(item)

    }

}

private class AsyncTaskRunner(val context: Context) : AsyncTask<String, String, String>(){

    internal lateinit var db: DatabaseHandler
    internal lateinit var pgd: ProgressDialog
    var resp:String? = null
    var cancel:String? = null


    override fun doInBackground(vararg params: String?): String {
        import()
        return "OK"
    }

    override fun onPreExecute() {
        pgd = ProgressDialog(context)
        pgd.setMessage("Loading")
        pgd.setTitle("Importing Data")

        pgd.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel", DialogInterface.OnClickListener{
                dialog, which ->
            cancel = "stop"
            dialog.dismiss()
            cancel = null
        })
        pgd.show()
        pgd.setCancelable(false)

        super.onPreExecute()
    }

    override fun onPostExecute(result: String?) {
        pgd.dismiss()
        val dialog: AlertDialog.Builder = AlertDialog.Builder(context)
        dialog.setTitle("STATUS!!")
        println(com_check)
        if (com_check == "complete") {
            dialog.setMessage("IMPORT COMPLETE ")
        }
        if (com_check == "error") {
            dialog.setMessage("IMPORT ERROR!! After Article Code : " + code_check)
        }

        dialog.setNegativeButton("OK", DialogInterface.OnClickListener() { dialog, which ->
            dialog.dismiss()

        })
        dialog.show()
        pgd.setCancelable(false)
//        MasterData()
        super.onPostExecute(result)
    }

    fun import() {

        val filepath=file
        val cursor=context.contentResolver.openInputStream(android.net.Uri.parse(filepath.toString()))
        val db1=context.openOrCreateDatabase("database.db", Context.MODE_PRIVATE, null)
//            lbl.text=filepath.toString()
//        master_path=filepath.toString()
        noti=cursor.toString()
        val tableName="master"
        db1.execSQL("delete from $tableName")
        val text =  StringBuilder()
        try {
            try {
                val file=InputStreamReader(cursor)
                var lineCount = 0
                val buffer=BufferedReader(file)
                val contentValues= ContentValues()
                db1.beginTransaction()

                while(true) {

                    lineCount++
                    in_line = lineCount

                    val line = buffer.readLine()
                    if (line == null) break

                    val str = line.split(",".toRegex())
                        .toTypedArray()
                    println("str    "+ line)

                    var barcode:String? = null
                    var description:String? = null
                    var weight:String? = null
                    var date:String? = null
                    var number:String? = null

                    barcode = str[0]
                    description = str[1]
                    weight = str[2]
                    date = str[3]
                    number = str[4]

                    contentValues.put("barcode", barcode)
                    contentValues.put("description", description)
                    contentValues.put("weight", weight)
                    contentValues.put("date", date)
                    contentValues.put("number", number)
                    db1.insert(tableName, null, contentValues)
                }
                com_check = "complete"
                println("CHECK"+ com_check)
                println(in_line.toString())


                db1.setTransactionSuccessful()
                db1.endTransaction()
            } catch (e: IOException) {
                if (db1.inTransaction())
                    db1.endTransaction()
                println("ERROR")
                com_check = "error"
                println(e)
            }

        } catch (ex: Exception) {
            if (db1.inTransaction())
                db1.endTransaction()
            println("ERROR!!!")
            com_check = "error"
            println(ex)
        }
        db1.close()

    }



}

