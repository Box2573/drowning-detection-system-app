package com.example.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.util.DisplayMetrics
import android.util.Log
import android.widget.EditText
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

class EmployeeAdapter(private val datalist: ArrayList<EmployeeData>): RecyclerView.Adapter<EmployeeAdapter.ViewHolderClass>()
{

    val client = OkHttpClient()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.employee_item, parent, false)
        return EmployeeAdapter.ViewHolderClass(itemView)
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
        holder.phone.text = currentItem.phone
        holder.role.text = currentItem.role

        holder.editBtn.setOnClickListener{
            if(position != 0)
            {
                showDialog(holder.itemView.context, holder.id.text.toString(), holder.name.text.toString(),
                    holder.phone.text.toString(), holder.role.text.toString())
            }
        }

    }

    private fun showDialog(context: Context, i: String, n: String, p: String, r: String)
    {
        val builder = AlertDialog.Builder(context)
        val inflater = (context as AppCompatActivity).layoutInflater
        val dialogView = inflater.inflate(R.layout.employee_update, null)

        val dismissBtn:Button = dialogView.findViewById(R.id.dismiss_button)
        val confirmBtn: Button = dialogView.findViewById(R.id.confirm_button)
        val deleteBtn: ImageButton = dialogView.findViewById(R.id.delete_button)

        val id: EditText = dialogView.findViewById(R.id.id_input)
        val name: EditText = dialogView.findViewById(R.id.name_input)
        val phone: EditText = dialogView.findViewById(R.id.phone_input)
        val role: EditText = dialogView.findViewById(R.id.role_input)

        id.setText(i)
        name.setText(n)
        phone.setText(p)
        role.setText(r)

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
            updateData(i, name.text.toString(), phone.text.toString(), role.text.toString(), dialog, context)
        }

        //刪除
        deleteBtn.setOnClickListener {
            val alertBuilder = AlertDialog.Builder(context)
            val dialogView2 = inflater.inflate(R.layout.delete_data, null)

            val confirmBtn: Button = dialogView2.findViewById(R.id.confirm_button)
            val dismissBtn: Button = dialogView2.findViewById(R.id.dismiss_button)
            val text: TextView = dialogView2.findViewById(R.id.text)

            alertBuilder.setView(dialogView2)
            val alertDialog = alertBuilder.create()
            alertDialog.show()

            text.text = "確定將員工$i" + "刪除?"

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
        val action = "deleteEmployee"
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
                        (context as PeopleManagementAdmin).getData("0")
                    }
                    else
                    {
                        Toast.makeText(context, "刪除失敗，請稍後再試", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateData(id: String, name: String, phone: String, role: String, dialog: AlertDialog, context: Context)
    {
        val baseUrl = context.getString(R.string.url)
        val action = "updateEmployee"
        val url = "$baseUrl?action=$action&id=$id&name=$name&phone=$phone&role=$role"

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
                        (context as PeopleManagementAdmin).getData("0")
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
        val id: TextView = itemView.findViewById(R.id.employee_id)
        val name: TextView = itemView.findViewById(R.id.name)
        val phone: TextView = itemView.findViewById(R.id.phone)
        val role: TextView = itemView.findViewById(R.id.role)
        val editBtn: ImageButton = itemView.findViewById(R.id.imageView)
    }

}