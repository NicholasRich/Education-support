package com.cniao.test_application.ui.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.cniao.test_application.R
import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.cniao.test_application.MainActivity
import com.cniao.test_application.ui.dashboard.DashboardFragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*



class LoginActivity : AppCompatActivity() {

    lateinit var btn1:Button
    lateinit var btn2:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn1 = findViewById(R.id.Login)
        btn2 = findViewById(R.id.Register)

        btn1.setOnClickListener {
            usersLogin()
        }
        btn2.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
    }

    private fun validateLogin(): Boolean {
        return when  {
            TextUtils.isEmpty(Et_1.text.toString().trim { it <= ' '}) -> {
                false
            }
            TextUtils.isEmpty(Et_2.text.toString().trim { it <= ' '}) -> {
                false
            }
            else -> {
                //showErrorSnackBar("Your details are valid.", false)
                true
            }
        }

    }

    private fun usersLogin(){
        if(validateLogin()) {
            val email = Et_1.text.toString().trim { it <= ' ' }
            val password = Et_2.text.toString().trim { it<= ' ' }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(
                            this@LoginActivity,
                            "You are logged in successfully!",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this,MainActivity::class.java)
                        intent.putExtra("email",email)//向intent中传递键值对 key--1024
                        startActivity(intent)
                        //startActivity(Intent(this, MainActivity::class.java))
                        //val transaction = supportFragmentManager.beginTransaction()
                        //transaction.replace(R.id., DashboardFragment()).commit()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "You are logged failed, please check the email and password!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}