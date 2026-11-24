package com.example.app

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DataReport : AppCompatActivity()
{
    lateinit var startDateBtn: Button
    lateinit var endDateBtn: Button
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_report)
        switchMode = findViewById(R.id.switch_mode)
        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中
        nightMode = sharedPerferences.getBoolean("nightMode", false)//把sharedPerferences資料裡的nightMode給nightMode，如果saredPerferences沒有該資料預設回傳false

        //region 嵌入式網頁
        val webView: WebView = findViewById(R.id.webView)
        webView.webViewClient = WebViewClient()
        webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(getString(R.string.web_url))
        //endregion

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

            val itemSelected = adapterView.getItemAtPosition(i)
            Toast.makeText(this, "選擇 $itemSelected", Toast.LENGTH_SHORT).show()
            autoComplete.setText("")

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

}