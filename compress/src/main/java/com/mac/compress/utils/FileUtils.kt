package com.mac.compress.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * @author ex-yangjb001
 * @date 2018/12/4.
 */
class FileUtils() {

    companion object {
        private const val FILE_PROVIDER_AUTHORITY = "com.iceteck.silicompressor.provider"

        fun getExtension(uri: String): String? {
            if (uri == null) {
                return null
            }
            val dot = uri.lastIndexOf(".")
            return if (dot >= 0) {
                uri.substring(dot)
            } else {
                ""
            }
        }

        fun isLocal(url: String): Boolean {
            return url != null && !url.startsWith("http://") && !url.startsWith("https://")
        }

        fun isMeidaUri(uri: Uri): Boolean {
            return "media".equals(uri.authority, true)
        }

        fun getUri(context: Context, file: File): Uri? {
            return if (file != null) {
                FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            } else {
                null
            }
        }
    }
}