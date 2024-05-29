package uikit.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import com.tonapps.uikit.color.backgroundContentColor
import com.tonapps.uikit.list.ListCell
import uikit.drawable.CellBackgroundDrawable

fun ListCell.Position.drawable(
    context: Context,
    backgroundColor: Int = context.backgroundContentColor,
    radius: Float = 16f.dp
): Drawable {
    return CellBackgroundDrawable.create(context, this, backgroundColor, radius)
}