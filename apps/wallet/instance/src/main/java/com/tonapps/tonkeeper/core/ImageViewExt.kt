package com.tonapps.tonkeeper.core
import android.net.Uri
import android.widget.ImageView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.transform.Transformation

fun ImageView.loadUri(
    uri: Uri,
    vararg transformations: Transformation
) {
    val memoryCache = MemoryCache.Builder(context)
        .maxSizePercent(0.1)
        .build()
    val diskCache = DiskCache.Builder()
        .directory(context.cacheDir.resolve("image_cache"))
        .maxSizePercent(0.05)
        .build()
    val imageLoader = ImageLoader.Builder(context)
        .components { add(SvgDecoder.Factory()) }
        .memoryCache(memoryCache)
        .diskCache(diskCache)
        .build()

    val request = ImageRequest.Builder(context)
        .data(uri)
        .transformations(*transformations)
        .target(this)
        .build()

    imageLoader.enqueue(request)
}