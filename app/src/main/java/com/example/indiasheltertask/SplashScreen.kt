package com.example.indiasheltertask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.indiasheltertask.utils.PrefConstants
import com.example.indiasheltertask.utils.SharedPref

class SplashScreen : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        if (isInternetConnected()) {
            if (hasRequiredPermissions()) {
                proceedToNextActivity()
            } else {
                requestPermissions()
            }
        } else {
            showInternetDialog()
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
            PERMISSIONS_REQUEST_CODE
        )
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    private fun showInternetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Internet Required")
            .setMessage("Please enable internet to continue.")
            .setPositiveButton("OK") { _, _ ->
                finish() // Dismiss the activity
            }
            .setCancelable(false)
            .show()
    }

    private fun proceedToNextActivity() {
        val isUserLoggedIn = SharedPref.getBoolean(PrefConstants.IS_USER_LOGGED_IN)
        val nextActivity = if (isUserLoggedIn) {
            ContactHome::class.java
        } else {
            ContactHome::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                if (isInternetConnected()) {
                    proceedToNextActivity()
                } else {
                    showInternetDialog()
                }
            } else {
                if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                    if (permissions.indices.any {
                            grantResults[it] == PackageManager.PERMISSION_DENIED &&
                                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[it])
                        }) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        requestPermissions()
                    }
                }
            }
        }
    }
}
