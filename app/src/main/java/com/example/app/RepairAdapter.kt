package com.example.app

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RepairAdapter(private val datalist: ArrayList<RepairData>): RecyclerView.Adapter<RepairAdapter.ViewHolderClass>()
{
    val client = OkHttpClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.repair_item, parent, false)
        return RepairAdapter.ViewHolderClass(itemView)
    }

    override fun getItemCount(): Int
    {
        return datalist.size
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int)
    {
        val currentItem = datalist[position]

        holder.id.text = currentItem.id
        holder.name.text = currentItem.name
        holder.date.text = currentItem.date
        holder.note.text = currentItem.note

        holder.editBtn.setOnClickListener{
            if(position != 0)
            {
                showDialog(holder.itemView.context, holder.id.text.toString(), holder.name.text.toString(),
                    holder.date.text.toString(), holder.note.text.toString())
            }
        }
    }

    private fun showDialog(context: Context, i: String, n: String, d: String, no: String)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.repair_update, null)

        val dismissBtn: Button = dialogView.findViewById(R.id.dismiss_button)
        val confirmBtn: Button = dialogView.findViewById(R.id.confirm_button)
        val deleteBtn: ImageButton = dialogView.findViewById(R.id.delete_button)

        val id: EditText = dialogView.findViewById(R.id.id_input)
        val name: EditText = dialogView.findViewById(R.id.name_input)
        val date: EditText = dialogView.findViewById(R.id.date_input)
        val note: EditText = dialogView.findViewById(R.id.note_input)

        id.setText(i)
        name.setText(n)
        date.setText(d)
        note.setText(no)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()
        dialog.window?.setLayout(2230, 510)

        //取消
        dismissBtn.setOnClickListener{
            dialog.dismiss()
        }

        //確定
        confirmBtn.setOnClickListener {
            updateData(i, name.text.toString(), date.text.toString(),note.text.toString(), dialog, context)
        }

        deleteBtn.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            val dialogView2 = inflater.inflate(R.layout.delete_data, null)

            val confirmBtn: Button = dialogView2.findViewById(R.id.confirm_button)
            val dismissBtn: Button = dialogView2.findViewById(R.id.dismiss_button)
            val text: TextView = dialogView2.findViewById(R.id.text)

            alertBuilder.setView(dialogView2)
            val alertDialog = alertBuilder.create()
            alertDialog.show()

            text.text = "確定將維修資料$i" + "刪除?"

            dismissBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            confirmBtn.setOnClickListener{
                deleteData(i, dialog, alertDialog,context)
            }
        }
    }

    private fun deleteData(i: String, dialog: AlertDialog, alertDialog: AlertDialog, context: Context)
    {
        val baseUrl = context.getString(R.string.url)
        val action = "deleteRepair"
        val url = "$baseUrl?action=$action&id=$i"

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
                (context as AppCompatActivity).runOnUiThread {
                    if (response.isSuccessful)
                    {
                        Toast.makeText(context, "刪除成功", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                        dialog.dismiss()
                        (context as RepairDataEdit).getData("0")
                    }
                    else
                    {
                        Toast.makeText(context, "刪除失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateData(i: String, name: String, date: String, note: String, dialog: AlertDialog, context: Context)
    {
        val baseUrl = context.getString(R.string.url)
        val action = "updateRepair"
        val url = "$baseUrl?action=$action&id=$i&name=$name&date=$date&note=$note"

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
                (context as AppCompatActivity).runOnUiThread {
                    if (response.isSuccessful)
                    {
                        Toast.makeText(context, "更新成功", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        (context as RepairDataEdit).getData("0")
                    }
                    else
                    {
                        Toast.makeText(context, "更新失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    class ViewHolderClass(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        val id: TextView = itemView.findViewById(R.id.repair_id)
        val name: TextView = itemView.findViewById(R.id.repair_name)
        val date: TextView = itemView.findViewById(R.id.repair_date)
        val note: TextView = itemView.findViewById(R.id.repair_note)
        val editBtn: ImageButton = itemView.findViewById(R.id.imageView)
    }
}