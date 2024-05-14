package uikit.base

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tonapps.uikit.icon.UIKitIcon
import uikit.R
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.topScrolled
import uikit.widget.HeaderView

open class BaseListFragment: BaseFragment(R.layout.fragment_list) {

    protected lateinit var headerView: HeaderView
    private lateinit var listView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        if (this is SwipeBack) {
            headerView.setIcon(UIKitIcon.ic_chevron_left_16)
            headerView.doOnCloseClick = { finish() }
        } else if (this is BottomSheet) {
            headerView.setAction(UIKitIcon.ic_close_16)
            headerView.doOnActionClick = { finish() }
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

    fun setTitle(title: String) {
        headerView.title = title
    }
}