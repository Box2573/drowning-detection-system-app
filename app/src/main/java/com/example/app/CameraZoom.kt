package com.example.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

class CameraZoom : AppCompatActivity()
{
    lateinit var switchMode: SwitchCompat
    var nightMode = false
    lateinit var sharedPerferences: SharedPreferences//存儲獲得資料
    lateinit var editor: SharedPreferences.Editor//用來保存資料

    private var currentStreamThread: Thread? = null
    private var isStreaming = AtomicBoolean(false)
    private var shouldStop = false



    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_zoom)

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
            val i = Intent(this, CameraView::class.java)
            i.putExtra("user", intent.getStringExtra("user"))
            startActivity(i)
        }

        val camerId: TextView = findViewById(R.id.camera_number_text)
        val cameraView: ImageView = findViewById(R.id.camera_view)
        val lastCamera: ImageButton = findViewById(R.id.left_arrow)
        val nextCamera: ImageButton = findViewById(R.id.right_arrow)
        var id = intent.getStringExtra("cameraId")?.toInt()

        camerId.text = "監視器00${id?.plus(1)}"
        getVideo(cameraView, "${intent.getStringExtra("cameraId")}")

        lastCamera.setOnClickListener{
            id = if (id == 0)
            {
                3
            }
            else
            {
                id?.minus(1)
            }

            getVideo(cameraView, id.toString())
            camerId.text = "監視器00${id?.plus(1)}"
        }

        nextCamera.setOnClickListener{
            id = if (id == 3)
            {
                0
            }
            else
            {
                id?.plus(1)
            }

            getVideo(cameraView, id.toString())
            camerId.text = "監視器00${id?.plus(1)}"
        }
    }

    fun getVideo(imageView: ImageView, id: String)
    {
        val baseUrl = getString(R.string.stream_url)
        val url = "$baseUrl$id"

        shouldStop = true

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
                response.body?.byteStream()?.let { inputStream ->
                    // 停止當前的影像流
                    shouldStop = false
                    stopCurrentStream()
                    // 確保每個影像流使用獨立的處理執行緒
                    currentStreamThread = Thread {
                        processMjpegStream(inputStream, imageView)
                    }
                    currentStreamThread?.start()
                }
            }
        })
    }

    private fun stopCurrentStream()
    {
        // 中止目前影像流處理
        isStreaming.set(false)

        // 中斷目前執行的 Thread
        currentStreamThread?.interrupt()
        currentStreamThread = null
    }

    private fun processMjpegStream(inputStream: java.io.InputStream, imageView: ImageView)
    {
        val boundary = "--frame\r\n".toByteArray()
        val buffer = ByteArray(4096)
        var currentFrame = ByteArrayOutputStream()
        val mainHandler = Handler(Looper.getMainLooper())

        var bytesRead: Int
        var inFrame = false

        while (inputStream.read(buffer).also { bytesRead = it } != -1)
        {
            if (shouldStop)
            {
                inputStream.close() // 關閉輸入流來停止影片
                return // 停止處理
            }
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

    override fun onDestroy()
    {
        super.onDestroy()
        shouldStop = true // 在 Activity 銷毀時停止所有影片流
    }
}