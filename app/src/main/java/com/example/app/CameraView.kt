package com.example.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException

class CameraView : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_view)

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

        val lastPage: ImageButton = findViewById(R.id.last_page)
        lastPage.setOnClickListener {
            val i = Intent(this, HomepageUser::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        var camera001: ImageView = findViewById(R.id.cameraView001)
        var camera002: ImageView = findViewById(R.id.cameraView002)
        var camera003: ImageView = findViewById(R.id.cameraView003)
        var camera004: ImageView = findViewById(R.id.cameraView004)

        getVideo(camera001, "0")
        getVideo(camera002, "1")
        getVideo(camera003, "2")
        getVideo(camera004, "3")

        var cv001: CardView = findViewById(R.id.cv001)
        var cv002: CardView = findViewById(R.id.cv002)
        var cv003: CardView = findViewById(R.id.cv003)
        var cv004: CardView = findViewById(R.id.cv004)

        cv001.setOnClickListener{
            var i = Intent(this, CameraZoom::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            i.putExtra("cameraId", "0")
            startActivity(i)
        }

        cv002.setOnClickListener{
            var i = Intent(this, CameraZoom::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            i.putExtra("cameraId", "1")
            startActivity(i)
        }

        cv003.setOnClickListener{
            var i = Intent(this, CameraZoom::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            i.putExtra("cameraId", "2")
            startActivity(i)
        }

        cv004.setOnClickListener{
            var i = Intent(this, CameraZoom::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            i.putExtra("cameraId", "3")
            startActivity(i)
        }


    }

    fun getVideo(imageView: ImageView, id: String)
    {
        val baseUrl = getString(R.string.stream_url)
        val url = "$baseUrl$id"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object: Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                print("fail");
            }

            override fun onResponse(call: Call, response: Response)
            {
                //獲得mjepg串流資料，並且在確定不是null後把獲得的資料inputStream傳給processMjpegStream
                // 並把imageView一起傳過去
                response.body?.byteStream()?.let { inputStream ->
                    // 確保每個影像流使用獨立的處理執行緒
                    Thread {
                        processMjpegStream(inputStream, imageView)
                    }.start()
                }
            }
        })
    }

    private fun processMjpegStream(inputStream: java.io.InputStream, imageView: ImageView)
    {
        val boundary = "--frame\r\n".toByteArray()
        val buffer = ByteArray(4096)
        var currentFrame = ByteArrayOutputStream()
        val mainHandler = Handler(Looper.getMainLooper())

        var bytesRead: Int
        var inFrame = false

        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            for (i in 0 until bytesRead)
            {
                if (inFrame)
                {
                    currentFrame.write(buffer[i].toInt())

                    if (i > 0 && buffer[i - 1] == 0xFF.toByte() && buffer[i] == 0xD9.toByte())
                    {
                        val imageBytes = currentFrame.toByteArray()
                        currentFrame.reset()
                        inFrame = false

                        // 確保影像更新是針對各自的 ImageView
                        mainHandler.post {
                            try
                            {
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                if (bitmap != null)
                                {
                                    imageView.setImageBitmap(bitmap)
                                }
                                else
                                {
                                    android.util.Log.e("MJPEG", "Bitmap is null")
                                }
                            }
                            catch (e: Exception)
                            {
                                android.util.Log.e("MJPEG", "Error decoding image", e)
                            }
                        }
                    }
                }
                else
                {
                    if (i < bytesRead - 1 && buffer[i] == 0xFF.toByte() && buffer[i + 1] == 0xD8.toByte())
                    {
                        inFrame = true
                        currentFrame.write(buffer[i].toInt())
                    }
                }
            }
        }
    }

}