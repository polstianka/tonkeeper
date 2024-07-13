package com.tonapps.wallet.data.core

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.tonapps.extensions.cacheFolder
import com.tonapps.extensions.file
import com.tonapps.extensions.folder
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toListParcel
import java.io.File
import kotlin.math.min

class ScreenCacheSource(
    context: Context
) {

    private val rootFolder = context.cacheFolder("screen")

    inline fun <reified T: Parcelable> get(
        name: String,
        walletId: Long,
        block: (parcel: Parcel) -> T
    ): List<T> {
        val bytes = getData(name, walletId)
        if (bytes.isEmpty()) {
            return emptyList()
        }
        val l = bytes.toListParcel(block) ?: emptyList()
        return l
    }

    fun getData(
        name: String,
        walletId: Long,
    ): ByteArray {
        val file = getFile(name, walletId)
        if (!file.exists() || file.length() == 0L) {
            return ByteArray(0)
        }
        return file.readBytes()
    }

    fun set(
        name: String,
        walletId: Long,
        list: List<Parcelable>
    ) {
        val file = getFile(name, walletId)
        if (list.isEmpty()) {
            file.delete()
        } else {
            val maxListSize = min(list.size, 15)
            val bytes = list.subList(0, maxListSize).toByteArray()
            file.writeBytes(bytes)
        }
    }

    private fun getFolder(name: String): File {
        return rootFolder.folder(name)
    }

    private fun getFile(name: String, walletId: Long): File {
        val folder = getFolder(name)
        val filename = "$walletId.dat"
        return folder.file(filename)
    }
}