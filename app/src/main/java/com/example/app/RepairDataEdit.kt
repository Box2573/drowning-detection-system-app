package com.example.app

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RepairDataEdit : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料
    val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repair_data_edit)

        switchMode = findViewById(R.id.switch_mode)
        sharedPerferences = getSharedPreferences("MODE", Context.MODE_PRIVATE)//取得資料，資料儲存在MODE中
        nightMode = sharedPerferences.getBoolean("nightMode", false)//把sharedPerferences資料裡的nightMode給nightMode，如果saredPerferences沒有該資料預設回傳false

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
            val i = Intent(this, DeviceManagement::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        getData("0")

        val searchBtn: ImageButton = findViewById(R.id.search_button)
        val searchBar: EditText = findViewById(R.id.search_input)

        searchBtn.setOnClickListener{
            if(searchBar.text.toString() == "")
            {
                getData("0")
            }
            else
            {
                getData(searchBar.text.toString())
            }
        }

        val addBtn: Button = findViewById(R.id.add_button)
        addBtn.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            val item = LayoutInflater.from(this).inflate(R.layout.add_repair, null)

            val confirm: Button = item.findViewById(R.id.confirm_button)
            val dismiss: Button = item.findViewById(R.id.dismiss_button)

            val idInput: EditText = item.findViewById(R.id.id_input)
            val nameInput: EditText = item.findViewById(R.id.name_input)
            val dateInput: EditText = item.findViewById(R.id.date_input)
            val noteInput: EditText = item.findViewById(R.id.note_input)

            builder.setView(item)
            val dialog = builder.create()
            dialog.show()

            dismiss.setOnClickListener{
                dialog.dismiss()
            }

            confirm.setOnClickListener{
                createData(idInput.text.toString(), nameInput.text.toString(), dateInput.text.toString(), noteInput.text.toString(), dialog)

            }

        }

    }

    private fun createData(id: String, name: String, date: String, note: String, dialog: AlertDialog)
    {
        val baseUrl = getString(R.string.url)
        val action = "createRepair"
        val url = "$baseUrl?action=$action&id=$id&name=$name&date=$date&note=$note"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                Log.d("request fail", e.toString())
            }

            override fun onResponse(call: Call, response: Response)
            {
                if (response.isSuccessful)
                {
                    runOnUiThread {
                        dialog.dismiss()
                        Toast.makeText(this@RepairDataEdit, "新增成功", Toast.LENGTH_LONG).show()
                        getData("0")
                    }
                }
                else
                {
                    runOnUiThread {
                        Toast.makeText(this@RepairDataEdit, "新增失敗", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    fun getData(id: String)
    {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("資料讀取中，請稍等...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        val baseUrl = getString(R.string.url)
        val action = "getRepair"
        val url = "$baseUrl?action=$action&id=$id"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                Log.d("request fail", e.toString())
            }

            override fun onResponse(call: Call, response: Response)
            {

                var dataList: ArrayList<RepairData> = arrayListOf<RepairData>()

                val responseData = response.body?.string()
                val jsonObj = JSONObject(responseData)
                val jsonArr: JSONArray = jsonObj.getJSONArray("item")

                //標題
                val repairData = RepairData("設備編號", "設備名稱", "維修日期", "備註")
                dataList.add(repairData)

                for (i in 0 until jsonArr.length())
                {
                    val item: JSONObject = jsonArr.getJSONObject(i)

                    val id = item.getString("deviceId")
                    val name = item.getString("name")
                    val date = item.getString("date")
                    val note = item.getString("note")

                    val repairData = RepairData(id, name, date, note)
                    dataList.add(repairData)
                }

                runOnUiThread{
                    progressDialog.dismiss()
                    recyclerView.adapter = RepairAdapter(dataList)
                }
            }
        })
    }
}