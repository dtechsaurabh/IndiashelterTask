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
                proceedToNextActivity()  // Both internet and permissions are okay
            } else {
                requestPermissions() // Permissions not granted, request them
            }
        } else {
            showInternetDialog() // Internet is not connected
        }
    }

    // Check if the required permissions are granted
    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    // Request the permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
            PERMISSIONS_REQUEST_CODE
        )
    }

    // Check if the device is connected to the internet
    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    // Show a dialog if the internet is not available
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

    // Proceed to the next activity if both permissions and internet are okay
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

    // Handle the result from the permissions request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, now check if the internet is connected
                if (isInternetConnected()) {
                    proceedToNextActivity() // Everything is OK, proceed
                } else {
                    showInternetDialog() // Internet not connected
                }
            } else {
                if (grantResults.any { it == PackageManager.PERMISSION_DENIED }) {
                    // Handle case where permissions are denied
                    if (permissions.indices.any {
                            grantResults[it] == PackageManager.PERMISSION_DENIED &&
                                    !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[it])
                        }) {
                        // User selected "Don't ask again"
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } else {
                        // Permissions denied, re-request
                        requestPermissions()
                    }
                }
            }
        }
    }
}
