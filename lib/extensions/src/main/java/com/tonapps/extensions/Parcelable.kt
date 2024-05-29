package com.tonapps.extensions

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.parcelableCreator

fun Parcelable.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

fun List<Parcelable>.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    forEach { it.writeToParcel(parcel, 0) }
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

fun ByteArray.createParcel(): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(this, 0, size)
    parcel.setDataPosition(0)
    return parcel
}

inline fun <reified T: Parcelable> ByteArray.toParcel(): T? {
    if (isEmpty()) {
        return null
    }
    return try {
        val parcel = createParcel()
        val creator = parcelableCreator<T>()
        val value = creator.createFromParcel(parcel)
        parcel.recycle()
        value
    } catch (e: Throwable) {
        null
    }
}

inline fun <reified T: Parcelable> ByteArray.toListParcel(): List<T>? {
    return try {
        val parcel = createParcel()
        val creator = parcelableCreator<T>()
        val list = mutableListOf<T>()
        while (parcel.dataAvail() > 0) {
            list.add(creator.createFromParcel(parcel))
        }
        parcel.recycle()
        list
    } catch (e: Throwable) {
        null
    }
}

inline fun <reified T: Parcelable> ByteArray.toListParcel(block: (parcel: Parcel) -> T): List<T>? {
    return try {
        val parcel = createParcel()
        val list = mutableListOf<T>()
        while (parcel.dataAvail() > 0) {
            list.add(block(parcel))
        }
        parcel.recycle()
        list
    } catch (e: Throwable) {
        null
    }
}
