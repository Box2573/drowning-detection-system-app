package com.example.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomepageAdmin : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料
    val client = OkHttpClient()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage_admin)

        val counts: TextView = findViewById(R.id.people_count)
        val date: TextView = findViewById(R.id.day)

        //region 下拉選單
        val items = listOf("${intent.getStringExtra("user")}", "設定", "登出")
        val autoComplete: AutoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        val adapter = ArrayAdapter(this, R.layout.drowndown_item_user, items)
        autoComplete.setAdapter(adapter)

        autoComplete.setOnClickListener {
            autoComplete.showDropDown()
        }

        autoComplete.onItemClickListener = AdapterView.OnItemClickListener{
                adapterView, view, i, l ->

            val login: Intent

            when(i)
            {
                //設定
                1->
                {
                    //設定
                }
                //登出
                2->
                {
                    login = Intent(this, Login::class.java)
                    startActivity(login)
                }
            }

            autoComplete.setText("")
        }
        //endregion

        //region Dark mode
        switchMode = findViewById(R.id.switch_mode)
        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中
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

        //region 按鈕區
        val cv001: CardView = findViewById(R.id.cv001)
        val cv002: CardView = findViewById(R.id.cv002)
        val cv003: CardView = findViewById(R.id.cv003)
        val cv004: CardView = findViewById(R.id.cv004)

        cv001.setOnClickListener {
            val i = Intent(this, PeopleManagementAdmin::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        cv002.setOnClickListener {
            val i = Intent(this, DeviceManagement::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        cv003.setOnClickListener {
            val i = Intent(this, DataReport::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        cv004.setOnClickListener {
            val i = Intent(this, Log::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }
        //endregion

        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MM/dd")
        val formattedDate = currentDate.format(formatter)
        date.text = formattedDate.toString()

        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中
        counts.text = sharedPerferences.getString("people", "0")//把sharedPerferences資料裡的nightMode給nightMode，如果sharedPerferences沒有該資料預設回傳false

        handler = Handler(Looper.getMainLooper())

        runnable = object : Runnable {
            override fun run() {
                getData(counts)
                handler.postDelayed(this, 5000)
            }
        }

        handler.post(runnable)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getData(counts: TextView)
    {
        val baseUrl = getString(R.string.url)
        val action = "getNumberOfAttendees"

        // 獲取當前日期
        val currentDate = LocalDate.now()
        // 定義日期格式
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        // 格式化日期
        val formattedDate = currentDate.format(formatter)

        android.util.Log.d("today", formattedDate)

        val url = "$baseUrl?action=$action&date=$formattedDate"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                android.util.Log.d("request fail", e.toString())
            }

            override fun onResponse(call: Call, response: Response)
            {
                val responseData = response.body?.string()
                val jsonObj = JSONObject(responseData)
                val jsonArr: JSONArray = jsonObj.getJSONArray("item")
                val c = jsonArr.getJSONObject(0).getInt("counts").toString()

                runOnUiThread {
                    editor = sharedPerferences.edit()
                    editor.putString("people", c)
                    editor.apply()
                    counts.text = c
                }

            }
        })
    }
}