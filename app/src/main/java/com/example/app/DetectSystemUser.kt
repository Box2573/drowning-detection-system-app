package com.example.app

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetectSystemUser : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料

    lateinit var startCardView: CardView
    lateinit var endCardView: CardView
    lateinit var startTxt: TextView
    lateinit var endTxt: TextView


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect_system_user)

        startCardView = findViewById(R.id.start_time_cardview)
        startTxt = findViewById(R.id.start_time_txt)
        endCardView = findViewById(R.id.end_time_cardview)
        endTxt = findViewById(R.id.end_time_txt)
        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中
        val myCalender = Calendar.getInstance()

        startTxt.text = sharedPerferences.getString("startTime", "選擇時間")
        endTxt.text = sharedPerferences.getString("endTime", "選擇時間")

        startCardView.setOnClickListener {
            showPickDialog(startTxt, myCalender, "start")
            Log.d("time", startTxt.text.toString())
        }

        endCardView.setOnClickListener{
            showPickDialog(endTxt, myCalender, "end")
            Log.d("time", endTxt.text.toString())
        }

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
            val i = Intent(this, HomepageUser::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }
    }

    fun showPickDialog(text: TextView, myCalender: Calendar, name: String)
    {
        val timeSetListener = TimePickerDialog.OnTimeSetListener{timePicker, hour, minute ->
            myCalender.set(Calendar.HOUR_OF_DAY, hour)
            myCalender.set(Calendar.MINUTE, minute)
            updateTime(text, myCalender, name)
        }

        TimePickerDialog(this, timeSetListener, myCalender.get(Calendar.HOUR_OF_DAY), myCalender.get(Calendar.MINUTE),
            true).show()
    }

    fun updateTime(text : TextView, myCalender: Calendar, name: String)
    {
        val format = "HH:mm"
        val sdf = SimpleDateFormat(format, Locale.TAIWAN)
        text.text = sdf.format(myCalender.time)

        val editor = sharedPerferences.edit()
        if(name == "start")
        {
            editor.putString("startTime", text.text.toString())
        }
        else
        {
            editor.putString("endTime", text.text.toString())
        }
        editor.apply()

    }
}