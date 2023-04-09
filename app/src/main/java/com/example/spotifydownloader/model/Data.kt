package com.example.spotifydownloader.model

import android.net.Uri
import android.os.Parcelable
import com.chaquo.python.PyObject
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Data(
    val songNames:@RawValue List<PyObject>,
    val concurrentDownloads: Int,
    val folderURI: Uri?

):Parcelable

