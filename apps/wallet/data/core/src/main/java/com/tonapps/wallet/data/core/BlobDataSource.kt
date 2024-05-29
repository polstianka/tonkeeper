package com.tonapps.wallet.data.core

import android.content.Context
import com.tonapps.extensions.cacheFolder
import com.tonapps.extensions.file
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

abstract class BlobDataSource<D>(
    context: Context,
    path: String,
    lruInitialCapacity: Int = 100
) {

    private val lruCache = ConcurrentHashMap<String, D>(lruInitialCapacity, 1.0f, 2)
    private val diskFolder = context.cacheFolder(path)

    fun getCache(key: String): D? {
        return getLruCache(key) ?: getDiskCache(key)
    }

    fun setCache(key: String, value: D) {
        setDiskCache(key, value)
    }

    fun updateCache(key: String, value: D): Boolean {   // true if cache changed
        return setDiskCache(key, value)
    }

    fun clearCache(key: String) {
        lruCache.remove(key)
        diskFile(key).delete()
    }

    private fun getLruCache(key: String): D? {
        return lruCache[key]
    }

    private fun setLruCache(key: String, value: D) {
        lruCache[key] = value
    }

    private fun getDiskCache(key: String): D? {
        val bytes = readDiskCache(key) ?: return null
        val value = onUnmarshall(bytes) ?: return null
        setLruCache(key, value)
        return value
    }

    private fun setDiskCache(key: String, value: D): Boolean {  // true if file changed
        val file = diskFile(key)

        val oldBytes: ByteArray? = try {
           file.readBytes()
        } catch (e: IOException) {
            null
        }

        val bytes = onMarshall(value)
        file.writeBytes(bytes)
        setLruCache(key, value)

        return !bytes.contentEquals(oldBytes)
    }

    private fun diskFile(key: String): File {
        return diskFolder.file("${key}.dat")
    }

    private fun readDiskCache(key: String): ByteArray? {
        val file = diskFile(key)
        return try {
            val bytes = file.readBytes()
            if (bytes.isEmpty()) {
                null
            } else bytes
        } catch (e: IOException) {
            null
        }
    }

    abstract fun onMarshall(data: D): ByteArray

    abstract fun onUnmarshall(bytes: ByteArray): D?

}