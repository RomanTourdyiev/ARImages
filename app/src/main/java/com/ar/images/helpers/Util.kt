package com.ar.images.helpers

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import androidx.core.content.ContentProviderCompat.requireContext
import com.ar.images.dao.ImageRoomDatabase
import com.ar.images.dao.ImageRoomEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.io.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URISyntaxException

/**
 * Created by Tourdyiev Roman on 24.06.2020.
 */
class Util {


    companion object {
        val instance = Util()
    }

    @Throws(URISyntaxException::class)
    fun getPath(context: Context?, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:".toRegex(), "")
                    }
                    val contentUriPrefixesToTry =
                        arrayOf(
                            "content://downloads/public_downloads",
                            "content://downloads/my_downloads",
                            "content://downloads/all_downloads"
                        )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id)
                        )
                        try {
                            return getDataColumn(
                                context,
                                contentUri,
                                null,
                                null
                            )
                        } catch (e: Exception) {
                        }
                    }
                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf<String?>(
                    split[1]
                )
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getDataColumn(
        context: Context?,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context?.contentResolver?.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getFileName(context: Context, fileUri: Uri): String {

        var name = ""
        val returnCursor = context?.contentResolver?.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    fun saveImage(context:Context, data: Intent?) {
        val uri = data?.data ?: return
        GlobalScope.launch(Dispatchers.IO) {
            getPath(context, uri)
                .let { it ->
                    var file: File? = null

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val parcelFileDescriptor = context
                            .contentResolver
                            .openFileDescriptor(uri, "r", null)

                        parcelFileDescriptor?.let {
                            val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                            file = File(
                                context.cacheDir,
                                instance.getFileName(context, uri)
                            )
                            val outputStream = FileOutputStream(file)
                            IOUtils.copy(inputStream, outputStream)
                        }
                    } else {
                        file = File(it)
                    }

                    file?.let {
                        val fileInputStream = FileInputStream(file)
                        val data = ByteArray(file!!.length().toInt())
                        val bufferedInputStream = BufferedInputStream(fileInputStream)
                        bufferedInputStream.read(data, 0, data.size)

                        var image = ImageRoomEntity()
                        image.apply {
                            title = file!!.name
                            width = 0.3F
                            bitmap = data
                        }

                        ImageRoomDatabase.getDB(context)?.imageRoomDao()?.insert(image)
                    }

                }
        }
    }
}