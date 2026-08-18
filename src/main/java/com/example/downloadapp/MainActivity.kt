package com.example.downloadapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.downloadapp.databinding.ActivityMainBinding
import com.example.downloadapp.service.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var downloadManager: DownloadManager
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        downloadManager = DownloadManager(this)

        setupUI()
        requestPermissions()
    }

    private fun setupUI() {
        binding.downloadButton.setOnClickListener {
            val url = binding.urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                downloadFile(url)
            } else {
                binding.statusText.text = "Please enter a valid URL"
            }
        }
    }

    private fun downloadFile(url: String) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.statusText.text = "Downloading..."
            binding.progressBar.progress = 0

            try {
                downloadManager.downloadFile(
                    url = url,
                    onProgress = { progress ->
                        binding.progressBar.progress = progress
                        binding.statusText.text = "Progress: $progress%"
                    },
                    onSuccess = { fileName ->
                        binding.statusText.text = "Downloaded: $fileName"
                        Log.d("Download", "File downloaded successfully: $fileName")
                    },
                    onError = { error ->
                        binding.statusText.text = "Error: $error"
                        Log.e("Download", "Download failed: $error")
                    }
                )
            } catch (e: Exception) {
                binding.statusText.text = "Error: ${e.message}"
                Log.e("Download", "Exception occurred", e)
            }
        }
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                Log.d("Permissions", "All permissions granted")
            } else {
                Log.d("Permissions", "Some permissions were denied")
            }
        }
    }
}
