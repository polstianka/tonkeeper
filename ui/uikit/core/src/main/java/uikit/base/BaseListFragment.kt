package uikit.base

import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.icon.UIKitIcon
import uikit.R
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.topScrolled
import uikit.widget.HeaderView

open class BaseListFragment: BaseFragment(R.layout.fragment_list) {

    private lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        if (this is SwipeBack) {
            headerView.setIcon(UIKitIcon.ic_chevron_left_16)
            headerView.doOnCloseClick = { finish() }
        }
        listView = view.findViewById(R.id.list)
        listView.applyNavBottomPadding(requireContext().getDimensionPixelSize(R.dimen.cornerMedium))

        collectFlow(listView.topScrolled, headerView::setDivider)
    }

    fun findListItemView(position: Int): View? {
        return listView.findViewHolderForAdapterPosition(position)?.itemView
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        listView.adapter = adapter
    }

    fun addItemDecoration(decoration: RecyclerView.ItemDecoration) {
        listView.addItemDecoration(decoration)
    }

    fun addScrollListener(listener: RecyclerView.OnScrollListener) {
        listView.addOnScrollListener(listener)
    }

    fun setTitle(title: String) {
        headerView.title = title
    }

    fun setActionIcon(@DrawableRes resId: Int, onClick: (view: View) -> Unit) {
        headerView.setAction(resId)
        headerView.doOnActionClick = onClick
    }
}