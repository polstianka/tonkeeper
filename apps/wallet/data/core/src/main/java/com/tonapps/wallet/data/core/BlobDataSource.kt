package com.tonapps.wallet.data.core

import android.content.Context
import android.os.Parcelable
import android.util.Log
import com.tonapps.extensions.cacheFolder
import com.tonapps.extensions.file
import com.tonapps.extensions.toByteArray
import com.tonapps.extensions.toParcel
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

abstract class BlobDataSource<D>(
    context: Context,
    path: String,
    private val timeout: Long = TimeUnit.DAYS.toMillis(90)
) {

    companion object {

        inline fun <reified T: Parcelable> simple(
            context: Context,
            path: String
        ): BlobDataSource<T> {
            return object : BlobDataSource<T>(context, path) {
                override fun onMarshall(data: T) = data.toByteArray()
                override fun onUnmarshall(bytes: ByteArray) = bytes.toParcel<T>()
            }
        }
    }

    private val diskFolder = context.cacheFolder(path)

    fun getCache(key: String): D? {
        return getDiskCache(key)
    }

    fun setCache(key: String, value: D) {
        setDiskCache(key, value)
    }

    fun clearCache(key: String) {
        diskFile(key).delete()
    }

    private fun getDiskCache(key: String): D? {
        val bytes = readDiskCache(key) ?: return null
        val value = onUnmarshall(bytes) ?: return null
        return value
    }

    private fun setDiskCache(key: String, value: D) {
        val file = diskFile(key)
        val bytes = onMarshall(value)
        file.writeBytes(bytes)
    }

    private fun diskFile(key: String): File {
        return diskFolder.file("${key}.dat")
    }

    private fun readDiskCache(key: String): ByteArray? {
        val file = diskFile(key)
        val lastModified = file.lastModified()
        if (file.length() == 0L || 0L >= lastModified) {
            return null
        }
        val diff = System.currentTimeMillis() - lastModified
        if (diff > timeout) {
            file.delete()
            return null
        }

        return try {
            val bytes = file.readBytes()
            if (bytes.isEmpty()) null else bytes
        } catch (e: IOException) {
            null
        }
    }

    abstract fun onMarshall(data: D): ByteArray

    abstract fun onUnmarshall(bytes: ByteArray): D?

}