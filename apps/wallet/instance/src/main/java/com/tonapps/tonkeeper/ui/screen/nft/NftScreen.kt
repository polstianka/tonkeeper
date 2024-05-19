package com.tonapps.tonkeeper.ui.screen.nft

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import com.tonapps.blockchain.ton.extensions.toUserFriendly
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.extensions.short4
import com.tonapps.tonkeeper.extensions.copyWithToast
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.textSecondaryColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.api.API
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.localization.Localization
import org.koin.android.ext.android.inject
import uikit.base.BaseFragment
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.drawable
import uikit.extensions.topScrolled
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView

class NftScreen: BaseFragment(R.layout.fragment_nft), BaseFragment.BottomSheet {

    private val api: API by inject()

    private val nftEntity: NftEntity by lazy {
        requireArguments().getParcelableCompat(ARG_ENTITY)!!
    }

    private val verificationIcon: Drawable by lazy {
        getDrawable(UIKitIcon.ic_verification_16, requireContext().accentBlueColor)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val headerView = view.findViewById<HeaderView>(R.id.header)
        headerView.title = nftEntity.name
        headerView.doOnCloseClick = { finish() }

        val contentView = view.findViewById<NestedScrollView>(R.id.content)
        contentView.applyNavBottomPadding()
        collectFlow(contentView.topScrolled, headerView::setDivider)

        val imageView = view.findViewById<FrescoView>(R.id.image)
        imageView.setImageURI(nftEntity.bigUri)

        val nameView = view.findViewById<AppCompatTextView>(R.id.name)
        nameView.text = nftEntity.name

        val collectionNameView = view.findViewById<AppCompatTextView>(R.id.collection_name)
        collectionNameView.text = nftEntity.collectionName.ifEmpty {
            getString(Localization.unnamed_collection)
        }
        if (nftEntity.verified) {
            collectionNameView.setCompoundDrawablesWithIntrinsicBounds(null, null, verificationIcon, null)
        } else {
            collectionNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        val descriptionView = view.findViewById<AppCompatTextView>(R.id.nft_description)
        if (nftEntity.description.isBlank()) {
            descriptionView.visibility = View.GONE
        } else {
            descriptionView.visibility = View.VISIBLE
            descriptionView.text = nftEntity.description
        }

        val aboutView = view.findViewById<View>(R.id.about)
        val collectionDescription = view.findViewById<AppCompatTextView>(R.id.collection_description)
        if (nftEntity.collectionDescription.isBlank()) {
            aboutView.visibility = View.GONE
        } else {
            aboutView.visibility = View.VISIBLE
            collectionDescription.text = nftEntity.collectionDescription
        }

        nftEntity.owner?.address?.let {
            setOwner(view, it)
        }
        setAddress(view, nftEntity.userFriendlyAddress)
    }

    private fun setOwner(view: View, address: String) {
        val ownerContainerView = view.findViewById<View>(R.id.owner_container)
        ownerContainerView.background = ListCell.Position.FIRST.drawable(requireContext())
        ownerContainerView.setOnClickListener {
            context?.copyWithToast(address)
        }

        val ownerAddressView = view.findViewById<AppCompatTextView>(R.id.owner)
        ownerAddressView.text = address.short4
    }

    private fun setAddress(view: View, address: String) {
        val explorerView = view.findViewById<AppCompatTextView>(R.id.open_explorer)
        explorerView.setOnClickListener {
            val explorerUrl = api.config.nftExplorer.format(address)
            navigation?.openURL(explorerUrl, true)
        }

        val addressContainerView = view.findViewById<View>(R.id.address_container)
        addressContainerView.background = ListCell.Position.LAST.drawable(requireContext())
        addressContainerView.setOnClickListener {
            context?.copyWithToast(address)
        }

        val addressView = view.findViewById<AppCompatTextView>(R.id.address)
        addressView.text = address.short4
    }

    companion object {

        private const val ARG_ENTITY = "entity"

        fun newInstance(entity: NftEntity): NftScreen {
            val fragment = NftScreen()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_ENTITY, entity)
            }
            return fragment
        }
    }
}