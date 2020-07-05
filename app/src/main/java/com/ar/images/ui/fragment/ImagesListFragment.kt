package com.ar.images.ui.fragment

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.ar.images.R
import com.ar.images.dao.ImageRoomDatabase
import com.ar.images.dao.ImageRoomEntity
import com.ar.images.helpers.Util
import com.ar.images.model.ImagesViewModel
import com.ar.images.ui.adapter.ImagesAdapter
import kotlinx.android.synthetic.main.fragment_intro.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


private const val REQUEST_PERMISSIONS = 101
private const val OPEN_FILE = 102

class ImagesListFragment : Fragment(), View.OnClickListener {

    private lateinit var imagesViewModel: ImagesViewModel
    private lateinit var imagesAdapter: ImagesAdapter

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.add_new_button -> {
                openFile()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // recycler
        imagesAdapter = ImagesAdapter()
        images_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = imagesAdapter
        }

        // toolbar
        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(toolbar, navHostFragment)

        // viewmodel
        imagesViewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
        imagesViewModel.getAllImages()
        imagesViewModel.imagesLiveData?.observe(viewLifecycleOwner, Observer { imagesList ->
            imagesAdapter.setData(imagesList)
        })

        //add_new button
        add_new_button.setOnClickListener(this)

    }


    fun openFile() {

        if (!requestCheckPermissions()) return

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Select an Image"),
                OPEN_FILE
            )
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(activity, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun requestCheckPermissions(): Boolean {

        val missingPermissions = ArrayList<String>()
        val requiredPermissions = getPermissionsNeeded()

        for (permission: String in requiredPermissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(permission)
            }
        }

        if (missingPermissions.size > 0) {
            val permissions = arrayOfNulls<String>(missingPermissions.size)
            requestPermissions(
                missingPermissions.toArray(permissions),
                REQUEST_PERMISSIONS
            )
            return false
        }
        return true
    }

    private fun getPermissionsNeeded(): ArrayList<String> {
        return arrayListOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        openFile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            Util.instance.saveImage(requireContext(), data)
        }
    }
}