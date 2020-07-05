package com.ar.images.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ar.images.R
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus.INSTALLED
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.android.synthetic.main.fragment_intro.*

private const val REQUEST_PERMISSIONS = 101

class IntroFragment : Fragment(), View.OnClickListener {

    private var mUserRequestedInstall = true

    override fun onClick(view: View?) {
        view?.let {
            when (view.id) {

                R.id.fragment_images_list_button -> {
                    view.findNavController()
                        .navigate(R.id.action_introFragment_to_imagesListFragment)
                }
                R.id.fragment_a_r_button -> {
                    view.findNavController().navigate(R.id.action_introFragment_to_ARActivity)
                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragment_images_list_button.setOnClickListener(this)
        fragment_a_r_button.setOnClickListener(this)

        if (checkARSupport()) {
            if (requestCheckPermissions()) {
                if (googlePlayServicesInstalled()) {

                }
            }
        }
    }

    fun checkARSupport(): Boolean {
        val availability = ArCoreApk.getInstance().checkAvailability(activity)

        if (availability.isTransient) {
            Handler().postDelayed({ checkARSupport() }, 200)
        }
        if (availability.isSupported) {
            a_r_not_supported.visibility = GONE
        } else {
            a_r_not_supported.visibility = VISIBLE
        }
        return availability.isSupported
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
        permissions_denied.visibility = GONE
        return true
    }

    private fun getPermissionsNeeded(): ArrayList<String> {
        return arrayListOf(
            Manifest.permission.CAMERA
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
        permissions_denied.visibility = GONE
    }

    fun googlePlayServicesInstalled(): Boolean {
        try {
            return when (ArCoreApk.getInstance().requestInstall(activity, mUserRequestedInstall)) {
                ArCoreApk.InstallStatus.INSTALLED -> {
                    play_services_not_installed.visibility = GONE
                    true
                }
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    mUserRequestedInstall = false

                    false
                }
                else -> {
                    false
                }
            }
        } catch (
            e: UnavailableUserDeclinedInstallationException
        ) {
            return false
        }
    }
}