package com.example.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class DrowningLogAdapter(private val datalist: ArrayList<DrowningLogData>): RecyclerView.Adapter<DrowningLogAdapter.ViewHolderClass>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.drowning_log_item, parent, false)
        return  ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int
    {
        return datalist.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int)
    {
        val currentItem = datalist[position]
        holder.date.text = currentItem.date
        holder.time.text = currentItem.time
        holder.cameraId.text = currentItem.cameraId

        holder.imageBtn.setOnClickListener{
            if(position != 0)
            {
                showImageDialog(holder.itemView.context, currentItem.image)
            }
        }
    }

    private fun showImageDialog(context: Context, url: Any)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_image, null)
        val imageView: ImageView = dialogView.findViewById(R.id.dialog_image_view)
        val positiveButton: Button = dialogView.findViewById(R.id.positive_button)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setLayout(1920, 1080)

        positiveButton.setOnClickListener {
            dialog.dismiss()
        }

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url.toString())
            .build()

        client.newCall(request).enqueue(object : Callback
        {
            override fun onFailure(call: Call, e: IOException)
            {
                (context as AppCompatActivity).runOnUiThread {
                    imageView.setImageResource(R.drawable.ic_image)
                }
            }

            override fun onResponse(call: Call, response: Response)
            {
                if (!response.isSuccessful)
                {
                    (context as AppCompatActivity).runOnUiThread {
                        imageView.setImageResource(R.drawable.ic_image)
                    }
                    return
                }

                val responseBody = response.body
                if (responseBody == null)
                {
                    (context as AppCompatActivity).runOnUiThread {
                        imageView.setImageResource(R.drawable.ic_image)
                    }
                    return
                }

                val inputStream = responseBody.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null)
                {
                    (context as AppCompatActivity).runOnUiThread {
                        imageView.setImageBitmap(bitmap)
                    }
                }
                else
                {
                    (context as AppCompatActivity).runOnUiThread {
                        imageView.setImageResource(R.drawable.ic_image)
                    }
                }

            }
        })
    }

    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val date: TextView = itemView.findViewById(R.id.date)
        val time: TextView = itemView.findViewById(R.id.time)
        val cameraId: TextView = itemView.findViewById(R.id.cameraID)
        val imageBtn: ImageButton = itemView.findViewById(R.id.imageView)
    }
}