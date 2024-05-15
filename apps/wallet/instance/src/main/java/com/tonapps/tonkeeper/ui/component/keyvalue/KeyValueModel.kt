package com.tonapps.tonkeeper.ui.component.keyvalue

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.tonapps.uikit.list.BaseListItem
import com.tonapps.uikit.list.ListCell
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class KeyValueModel : BaseListItem(), ListCell, Parcelable {
    data class Header(
        val key: String,
        val isLoading: Boolean,
        override val position: ListCell.Position,
    ) : KeyValueModel()

    data class Simple(
        val key: String,
        val value: String,
        @ColorRes val valueTint: Int? = null,
        override val position: ListCell.Position,
        val caption: String? = null,
    ) : KeyValueModel()
}