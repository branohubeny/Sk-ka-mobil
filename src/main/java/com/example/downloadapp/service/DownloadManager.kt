package com.example.downloadapp.service

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadManager(private val context: Context) {

    private val httpClient = OkHttpClient.Builder().build()

    suspend fun downloadFile(
        url: String,
        onProgress: (Int) -> Unit = {},
        onSuccess: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                onError("HTTP Error: ${response.code}")
                return@withContext
            }

            val body = response.body ?: run {
                onError("Empty response body")
                return@withContext
            }

            val contentLength = body.contentLength()
            val fileName = url.substringAfterLast("/").ifEmpty { "download_${System.currentTimeMillis()}" }
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: run {
                onError("Cannot access downloads directory")
                return@withContext
            }

            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val file = File(downloadDir, fileName)
            var downloadedBytes = 0L

            file.outputStream().use { fileOut ->
                body.byteStream().use { inputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        fileOut.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead

                        if (contentLength > 0) {
                            val progress = (downloadedBytes * 100 / contentLength).toInt()
                            onProgress(progress)
                        }
                    }
                }
            }

            onSuccess(file.absolutePath)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
        }
    }
}
