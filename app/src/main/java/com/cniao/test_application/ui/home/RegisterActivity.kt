package com.cniao.test_application.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cniao.test_application.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*
import com.cniao.test_application.ui.ViewModel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    lateinit var viewModel: UserViewModel
    lateinit var btn1: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_register)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        lifecycle.addObserver(viewModel)

        btn1 = findViewById(R.id.Bt_Register)
        btn1.setOnClickListener() {
            registerUser()
        }
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(ReEt_Et2.text.toString().trim { it <= ' ' }) -> {
                //(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(ReEt_Et1.text.toString().trim { it <= ' ' }) -> {
                //showErrorSnackBar(resources.getString(R.string.err_msg_enter_name), true)
                false
            }
            TextUtils.isEmpty(ReEt_Et3.text.toString().trim { it <= ' ' }) -> {
                //showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            TextUtils.isEmpty(ReEt_Et4.text.toString().trim { it <= ' ' }) -> {
                //showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }
            ReEt_Et3.text.toString().trim { it <= ' ' } != ReEt_Et4.text.toString()
                .trim { it <= ' ' } -> {
                //showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            /*
            !cb_terms_and_condition.isChecked -> {
                //showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }*/
            else -> {
                //showErrorSnackBar("Your details are valid! Registration Successfully!", false)
                true
            }
        }
    }

    private fun registerUser() {
        val db = Firebase.firestore

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        if (validateRegisterDetails()) {
            //showProgressDialog(resources.getString(R.string.please_wait))
            val password: String = ReEt_Et3.text.toString().trim { it <= ' ' }
            val email: String = ReEt_Et2.text.toString().trim { it <= ' ' }
            val name: String = ReEt_Et1.text.toString().trim { it <= ' ' }

            val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
            val selectedRadioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
            val identity = selectedRadioButton.text.toString()

            if (identity == "Educator") {
                Log.d("my identity is ",identity)
            } else if (identity == "Learner") {
                Log.d("my identity is ",identity)
            }
            //val identity = "leaner"

            val user = hashMapOf(
                "name" to name,
                "identity" to identity,
                "email" to email
            )


            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d("password", password)
                    if (task.isSuccessful) {
                        Log.d("test0", "test")
                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener { documentReference ->2
                                Log.d(
                                    TAG, "DocumentSnapshot added with ID: ${documentReference.id}"
                                )
                                Toast.makeText(
                                    this@RegisterActivity,
                                    resources.getString(R.string.register_success),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                        finish()
                    }
                    else
                    {
                        Toast.makeText(
                            this@RegisterActivity,
                            resources.getString(R.string.register_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}