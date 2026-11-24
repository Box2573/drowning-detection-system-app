package com.example.app

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AlertDialogLayout

class Login : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val forgetPass: TextView = findViewById(R.id.forget_password)

        val account: EditText = findViewById(R.id.user_input)
        val password: EditText = findViewById(R.id.password_input)
        val login: Button = findViewById(R.id.login_btn)

        login.setOnClickListener{
            val i: Intent

            if(account.text.toString() == "admin")
            {
                i= Intent(this, HomepageAdmin::class.java)
                i.putExtra("user", account.text.toString())
                startActivity(i)
            }
            else if (account.text.toString() == "user")
            {
                i= Intent(this, HomepageUser::class.java)
                i.putExtra("user", account.text.toString())
                startActivity(i)
            }
        }

        forgetPass.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("忘記密碼")
                .setMessage("請找管理員")
                .setPositiveButton("確定") { _, _ -> Toast.makeText(applicationContext, "記得找管理員", Toast.LENGTH_SHORT).show() }
                .show()
        }
    }
}