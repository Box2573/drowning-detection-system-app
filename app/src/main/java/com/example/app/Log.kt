package com.example.app

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager.Strings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Log : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料
    lateinit var pickDateBtn: Button
    var id = 0
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)
        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中

        //region 下拉選單
        val items = listOf("監視器001", "監視器002", "監視器003", "監視器004", "全部資訊")
        val autoComplete: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
        autoComplete.setAdapter(adapter)

        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }

        autoComplete.onItemClickListener = AdapterView.OnItemClickListener{
            adapterView, view, i, l ->

            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(this, "選擇 $itemSelected", Toast.LENGTH_SHORT).show()
            if(i == 4)
            {
                id = 0
            }
            else
            {
                id = i + 1
            }

            getData(id, "")
        }
        //endregion

        //region 下拉選單_user
        val textFild: TextInputLayout = findViewById(R.id.ic_account)
        val items2 = listOf("${intent.getStringExtra("user")}", "設定", "登出")
        val autoComplete2: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView_user)
        val adapter2 = ArrayAdapter(this, R.layout.drowndown_item_user, items2)
        autoComplete2.setAdapter(adapter2)

        autoComplete2.setOnClickListener {
            autoComplete2.showDropDown()
        }
        autoComplete2.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->

            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(this, "選擇 $itemSelected", Toast.LENGTH_SHORT).show()
            autoComplete2.setText("")

            when(i)
            {
                1->
                {
                    //setting
                }
                2->
                {
                    val i = Intent(this, Login::class.java)
                    startActivity(i)
                }
            }
        }
        //endregion

        //region 日期選擇
        val myCalender = Calendar.getInstance()
        pickDateBtn = findViewById(R.id.date_pick)
        pickDateBtn.setText(sharedPerferences.getString("pcikADay", "選擇日期"))

        pickDateBtn.setOnClickListener {
                showDatePickDialog(pickDateBtn, myCalender)
            }
        //endregion

        //region Dark mode
        switchMode = findViewById(R.id.switch_mode)
        nightMode = sharedPerferences.getBoolean("nightMode", false)//把sharedPerferences資料裡的nightMode給nightMode，如果sharedPerferences沒有該資料預設回傳false

        if(nightMode)
        {
            switchMode.isChecked = true
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//設成夜間模式
        }

        switchMode.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?)
            {
                if(nightMode)
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)//設成日間模式
                    editor = sharedPerferences.edit()
                    editor.putBoolean("nightMode", false)
                }
                else
                {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)//設成日間模式
                    editor = sharedPerferences.edit()
                    editor.putBoolean("nightMode", true)
                }
                editor.apply()
            }
        })
        //endregion

        val lastPage: ImageButton = findViewById(R.id.last_page)
        lastPage.setOnClickListener {
            if(intent.getStringExtra("user").toString() == "admin")
            {
                val i = Intent(this, HomepageAdmin::class.java)
                i.putExtra("user", intent.getStringExtra("user"))
                startActivity(i)
            }
            else if(intent.getStringExtra("user").toString() == "user")
            {
                val i = Intent(this, HomepageUser::class.java)
                i.putExtra("user", intent.getStringExtra("user"))
                startActivity(i)
            }
        }

    }

    private fun getData(id: Int, date: String)
    {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("請稍等...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        //region 取得資料
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val client = OkHttpClient()
        val baseUrl = getString(R.string.url)
        val action = "get"
        val url = "$baseUrl?action=$action&cameraID=$id&date=$date"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException)
            {
                Log.d("request fail", e.toString())
            }

            override fun onResponse(call: Call, response: Response)
            {

                var dataList: ArrayList<DrowningLogData> = arrayListOf<DrowningLogData>()

                val responseData = response.body?.string()
                val jsonObj = JSONObject(responseData)
                val jsonArr: JSONArray = jsonObj.getJSONArray("item")

                val drowningLogData = DrowningLogData("日期", "時間", "監視器編號", "圖片")
                dataList.add(drowningLogData)

                for (i in 0 until jsonArr.length())
                {
                    val item: JSONObject = jsonArr.getJSONObject(i)

                    val date = item.getString("date")
                    val time = item.getString("time")
                    val cameraId = item.getString("cameraID")
                    val image = item.getString("image")

                    val drowningLogData = DrowningLogData(date, time, cameraId, image)
                    dataList.add(drowningLogData)

                }

                runOnUiThread{
                    progressDialog.dismiss()
                    recyclerView.adapter = DrowningLogAdapter(dataList)
                }
            }
        })

        //endregion
    }

    private fun showDatePickDialog(btn: Button, myCalender: Calendar)
    {
        val datePicker = DatePickerDialog.OnDateSetListener{ _, year, month, _ ->
            myCalender.set(Calendar.YEAR, year)
            myCalender.set(Calendar.MONTH, month)
            updateLable(btn ,myCalender)
        }

        DatePickerDialog(this, datePicker, myCalender.get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
            myCalender.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateLable(btn: Button, myCalender: Calendar)
    {
        val format = "yyyy/MM"
        val sdf = SimpleDateFormat(format, Locale.TAIWAN)
        btn.setText(sdf.format(myCalender.time))

        val editor = sharedPerferences.edit()
        editor.putString("pcikADay", btn.text.toString())
        editor.apply()
        getData(id, sdf.format(myCalender.time).toString())
    }
}