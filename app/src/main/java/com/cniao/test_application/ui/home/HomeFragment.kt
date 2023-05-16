package com.cniao.test_application.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cniao.test_application.R
import com.cniao.test_application.databinding.FragmentHomeBinding
import com.cniao.test_application.ui.dashboard.DashboardFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.ByteArrayOutputStream


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.root.findViewById<Button>(R.id.LIST_LECTURES).visibility = View.INVISIBLE
        binding.root.findViewById<Button>(R.id.MY_LECTURES).visibility = View.INVISIBLE
        lateinit var btn1: Button
        lateinit var btn2: Button
        lateinit var btn3: Button
        lateinit var btn4: Button
        lateinit var vt1: TextView
        lateinit var vt2: TextView
        lateinit var et1: EditText
        val db = Firebase.firestore


        vt1 = root.findViewById(R.id.home_tv_1)
        vt2 = root.findViewById(R.id.home_tv_2)
        et1 = root.findViewById(R.id.home_ET_1)
        val imageView = root.findViewById<ImageView>(R.id.showImage_Login)

        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("images/th.jpg")
        val ONE_MEGABYTE: Long = 1024 * 1024
        imageRef.getBytes(ONE_MEGABYTE)
            .addOnSuccessListener { bytes ->
                // 将字节数组转换为 Bitmap 对象，并将其显示在 ImageView 中
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bitmap)
            }
            .addOnFailureListener { exception ->
                // 处理下载失败的情况
                Log.e(TAG, "Error downloading image: ${exception.message}", exception)
            }


        val Edit_Informatiom = et1.text.toString()
        val extraData = requireActivity().intent.getStringExtra("email")
        Log.d("MainActivity", "$extraData 已经被取出")
        db.collection("users").whereEqualTo("email", extraData)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data.get("name")}")
                    vt1.text = document.data.get("name").toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        db.collection("personal_information").whereEqualTo("name", "zhou")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(
                        TAG,
                        "personal_information${document.id} => ${document.data.get("information")}"
                    )
                    vt2.text = document.data.get("information").toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

        btn1 = root.findViewById(R.id.Edit_Informatiom)
        btn2 = root.findViewById(R.id.LIST_LECTURES)
        btn3 = root.findViewById(R.id.MY_LECTURES)
        btn4 = root.findViewById(R.id.LOGIN_OUT)


        imageView.setOnClickListener() {
            val imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.th)
            imageView.setImageBitmap(imageBitmap)
            save_Image()
        }

        et1.setOnClickListener() {
            if (et1.visibility == View.GONE) {
                et1.visibility = View.VISIBLE
            } else {
                et1.visibility = View.GONE
            }
        }
        btn1.setOnClickListener {
            //startActivity(Intent(activity, LoginActivity::class.java))
            if (et1.visibility == View.GONE) {
                et1.visibility = View.VISIBLE
            }
            val text = et1.text.toString()
            val information = hashMapOf(
                "information" to text,
                "name" to vt1.text
            )
            val collectionRef = db.collection("personal_information")
            collectionRef.whereEqualTo("name", vt1.text).get()
                .addOnSuccessListener {
                    for (item in it) {
                        item.id
                        val documentRef = collectionRef.document(item.id)
                        documentRef.set(information as Map<String, Any>)
                            .addOnSuccessListener {
                                // 更新成功
                                Log.d("update success", "update information success")
                            }
                            .addOnFailureListener { e ->
                                // 更新失败
                                Log.d("update failed", "update information success")
                            }
                    }
                }
            vt2.text = text
        }
        btn2.setOnClickListener {
            // Handle button click event for btn2
            requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
                .navigate(R.id.action_navigation_home_to_navigation_dashboard)
        }
        btn3.setOnClickListener {
            // Handle button click event for btn2
            //val transaction = requireActivity().supportFragmentManager.beginTransaction()
            //transaction.replace(R.id.container_main, DashboardFragment()).commit()
            requireActivity().findNavController(R.id.nav_host_fragment_activity_main)
                .navigate(R.id.action_navigation_home_to_navigation_notifications)
        }
        btn4.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }
        root.setOnClickListener {
            // Handle click event for the root view
        }
        return root
    }

    private fun save_Image() {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.th)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("images/th.jpg")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            Log.d(TAG, "Image uploaded successfully: ${taskSnapshot.metadata?.path}")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error uploading image: ${e.message}", e)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}