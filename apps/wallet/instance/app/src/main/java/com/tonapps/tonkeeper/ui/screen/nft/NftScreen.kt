package com.tonapps.tonkeeper.ui.screen.nft

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import com.tonapps.blockchain.ton.extensions.equalsAddress
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.extensions.short4
import com.tonapps.extensions.toUriOrNull
import com.tonapps.tonkeeper.extensions.copyWithToast
import com.tonapps.tonkeeper.extensions.isLightTheme
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeper.extensions.toastLoading
import com.tonapps.tonkeeper.koin.remoteConfig
import com.tonapps.tonkeeper.koin.walletViewModel
import com.tonapps.tonkeeper.popup.ActionSheet
import com.tonapps.tonkeeper.ui.base.WalletContextScreen
import com.tonapps.tonkeeper.ui.component.LottieView
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppArgs
import com.tonapps.tonkeeper.ui.screen.browser.dapp.DAppScreen
import com.tonapps.tonkeeper.ui.screen.root.RootViewModel
import com.tonapps.tonkeeper.ui.screen.send.main.SendScreen
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentBlueColor
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentRedColor
import com.tonapps.uikit.icon.UIKitIcon
import com.tonapps.uikit.list.ListCell
import com.tonapps.wallet.data.account.entities.WalletEntity
import com.tonapps.wallet.data.collectibles.entities.NftEntity
import com.tonapps.wallet.data.core.Trust
import com.tonapps.wallet.localization.Localization
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.core.parameter.parametersOf
import uikit.base.BaseFragment
import uikit.dialog.alert.AlertDialog
import uikit.extensions.applyNavBottomPadding
import uikit.extensions.collectFlow
import uikit.extensions.dp
import uikit.extensions.drawable
import uikit.extensions.getDimensionPixelSize
import uikit.extensions.inflate
import uikit.extensions.roundTop
import uikit.extensions.setRightDrawable
import uikit.extensions.topScrolled
import uikit.widget.ColumnLayout
import uikit.widget.FrescoView
import uikit.widget.HeaderView

class NftScreen(wallet: WalletEntity): WalletContextScreen(R.layout.fragment_nft, wallet), BaseFragment.BottomSheet {

    override val fragmentName: String = "NftScreen"

    private val nftEntity: NftEntity by lazy { requireArguments().getParcelableCompat(ARG_ENTITY)!! }

    private val isCanSend: Boolean
        get() = !wallet.isWatchOnly && !nftEntity.inSale && nftEntity.ownerAddress.equalsAddress(wallet.address)

    private val rootViewModel: RootViewModel by activityViewModel()

    override val viewModel: NftViewModel by walletViewModel { parametersOf(nftEntity) }

    private val verificationIcon: Drawable by lazy {
        getDrawable(UIKitIcon.ic_verification_16, requireContext().accentBlueColor)
    }

    private lateinit var headerView: HeaderView
    private lateinit var spamView: View
    private lateinit var previewView: FrameLayout

    private var lottieView: LottieView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.title = nftEntity.name
        headerView.doOnCloseClick = { finish() }

        val contentView = view.findViewById<NestedScrollView>(R.id.content)
        contentView.applyNavBottomPadding()
        collectFlow(contentView.topScrolled, headerView::setDivider)

        spamView = view.findViewById(R.id.spam)

        previewView = view.findViewById(R.id.preview)

        view.findViewById<Button>(R.id.report_spam).setOnClickListener { reportSpam(true) }
        view.findViewById<Button>(R.id.not_spam).setOnClickListener { reportSpam(false) }

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

        val transferButton = view.findViewById<Button>(R.id.transfer)
        transferButton.setOnClickListener {
            navigation?.add(SendScreen.newInstance(
                wallet = wallet,
                nftAddress = nftEntity.address
            ))
        }

        val domainLinkButton = view.findViewById<Button>(R.id.domain_link)
        val domainRenewButton = view.findViewById<Button>(R.id.domain_renew)

        if (nftEntity.isDomain && nftEntity.metadata.buttons.isEmpty()) {
            val url = Uri.parse("https://dns.tonkeeper.com/manage?v=${nftEntity.userFriendlyAddress}")
            val dAppArgs = DAppArgs(
                url = url,
                source = "nft",
                sendAnalytics = true
            )
            domainLinkButton.setOnClickListener {
                navigation?.add(DAppScreen.newInstance(wallet, dAppArgs))
                finish()
            }
            if (nftEntity.isTelegramUsername) {
                domainLinkButton.setBackgroundResource(uikit.R.drawable.bg_button_secondary)
                domainLinkButton.setTextColor(getColor(uikit.R.color.button_secondary_foreground_selector))
                domainRenewButton.visibility = View.GONE
            } else {
                domainRenewButton.visibility = View.VISIBLE
                domainRenewButton.setOnClickListener {
                    navigation?.add(DAppScreen.newInstance(wallet, dAppArgs))
                    finish()
                }
            }
        } else {
            domainLinkButton.visibility = View.GONE
            domainRenewButton.visibility = View.GONE
        }

        val buttonsContainer = view.findViewById<ColumnLayout>(R.id.buttons_container)
        if (nftEntity.metadata.buttons.isEmpty()) {
            buttonsContainer.visibility = View.GONE
        } else {
            buttonsContainer.removeAllViews()
            buttonsContainer.visibility = View.VISIBLE
            for ((index, button) in nftEntity.metadata.buttons.take(5).withIndex()) {
                val buttonView = newNftButton(buttonsContainer, index == 0)
                buttonView.text = button.label
                buttonView.setOnClickListener { openButtonDApp(button.uri) }
            }
        }

        val transferDisabled = view.findViewById<View>(R.id.transfer_disabled)

        if (!isCanSend) {
            transferButton.isEnabled = false
            transferDisabled.visibility = View.VISIBLE
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
        setTrust(nftEntity.trust)

        if (!wallet.isTonConnectSupported) {
            buttonsContainer.visibility = View.GONE
            domainLinkButton.visibility = View.GONE
            domainRenewButton.visibility = View.GONE
            transferButton.visibility = View.GONE
        }

        if (nftEntity.lottieUri != null) {
            lottieView = LottieView(requireContext()).apply {
                roundTop(16.dp)
                setUri(nftEntity.lottieUri!!)
            }
            previewView.addView(lottieView, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    private fun setTrust(trust: Trust) {
        when (trust) {
            Trust.whitelist -> showTrustState()
            Trust.graylist -> showGrayState()
            else -> showUnverifiedState()
        }
    }

    private fun openButtonDApp(url: String) {
        if (nftEntity.suspicious) {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(Localization.nft_warning_buttton_title)
            builder.setMessage(getString(Localization.nft_warning_buttton_subtitle, url))
            builder.setNegativeButton(Localization.open_anyway, requireContext().accentRedColor) { mustOpenButtonDApp(url) }
            builder.setPositiveButton(Localization.cancel, requireContext().accentBlueColor)
            builder.show()
        } else {
            mustOpenButtonDApp(url)
        }
    }

    private fun burn() {
        navigation?.add(SendScreen.newInstance(
            wallet = wallet,
            targetAddress = viewModel.burnAddress,
            nftAddress = nftEntity.address
        ))
        finish()
    }

    private fun mustOpenButtonDApp(url: String) {
        if (url.startsWith("ton:")) {
            val uri = url.toUriOrNull() ?: return
            rootViewModel.processDeepLink(uri, false, null, false, null)
        } else {
            navigation?.add(DAppScreen.newInstance(
                wallet = wallet,
                url = url.toUri(),
                source = "nft"
            ))
        }
        finish()
    }

    private fun newNftButton(parent: ColumnLayout, first: Boolean): Button {
        val isLight = requireContext().isLightTheme
        val layout = if (first) R.layout.view_nft_button_green else R.layout.view_nft_button
        val view = parent.context.inflate(layout)
        val button = view.findViewById<Button>(R.id.nft_button)
        button.isEnabled = isCanSend
        if (isLight) {
            button.setTextColor(Color.WHITE)
        }
        val iconView = view.findViewById<AppCompatImageView>(R.id.nft_button_icon)
        if (!isCanSend) {
            iconView.alpha = 0.5f
        }
        parent.addView(view, ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = requireContext().getDimensionPixelSize(uikit.R.dimen.offsetMedium)
            leftMargin = topMargin
            rightMargin = topMargin
        })
        return button
    }

    private fun showGrayState() {
        headerView.doOnActionClick = { showGrayMenu(it) }
    }

    private fun showGrayMenu(view: View) {
        val actionSheet = ActionSheet(requireContext())
        if (nftEntity.collection == null) {
            actionSheet.addItem(HIDE_NFT_ID, Localization.hide_collection, UIKitIcon.ic_eye_disable_16)
        } else {
            actionSheet.addItem(HIDE_NFT_ID, Localization.hide_full_collection, UIKitIcon.ic_eye_disable_16)
        }
        actionSheet.addItem(HIDE_AND_REPORT_ID, Localization.hide_and_report_collection, UIKitIcon.ic_block_16)
        actionSheet.addItem(VIEWER_ID, Localization.open_tonviewer, UIKitIcon.ic_globe_16)
        if (isCanSend && !nftEntity.isTrusted) {
            actionSheet.addItem(BURN_ID, Localization.burn, UIKitIcon.ic_fire_badge_16)
        }
        actionSheet.doOnItemClick = { item ->
            when (item.id) {
                HIDE_NFT_ID -> hideCollection()
                HIDE_AND_REPORT_ID -> reportSpam(true)
                VIEWER_ID -> openTonViewer()
                BURN_ID -> burn()
            }
        }
        actionSheet.show(view)
    }

    private fun showMenu(view: View) {
        val actionSheet = ActionSheet(requireContext())
        if (nftEntity.collection == null) {
            actionSheet.addItem(HIDE_NFT_ID, Localization.hide_collection, UIKitIcon.ic_eye_disable_16)
        } else {
            actionSheet.addItem(HIDE_NFT_ID, Localization.hide_full_collection, UIKitIcon.ic_eye_disable_16)
        }
        actionSheet.addItem(VIEWER_ID, Localization.open_tonviewer, UIKitIcon.ic_globe_16)
        if (isCanSend && !nftEntity.isTrusted) {
            actionSheet.addItem(BURN_ID, Localization.burn, UIKitIcon.ic_fire_badge_16)
        }
        actionSheet.doOnItemClick = { item ->
            when (item.id) {
                HIDE_NFT_ID -> hideCollection()
                VIEWER_ID -> openTonViewer()
                BURN_ID -> burn()
            }
        }
        actionSheet.show(view)
    }

    private fun showUnverifiedState() {
        spamView.visibility = View.VISIBLE
        val color = requireContext().accentOrangeColor
        val icon = requireContext().drawable(UIKitIcon.ic_information_circle_16, color)

        headerView.setSubtitle(Localization.nft_unverified)
        headerView.doOnActionClick = { showGrayMenu(it) }
        with(headerView.subtitleView) {
            visibility = View.VISIBLE
            compoundDrawablePadding = 8.dp
            setTextColor(color)
            setRightDrawable(icon)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lottieView?.destroy()
        lottieView = null
    }

    private fun showTrustState() {
        spamView.visibility = View.GONE
        headerView.setSubtitle(null)
        headerView.doOnActionClick = { showMenu(it) }
    }

    private fun reportSpam(spam: Boolean) {
        navigation?.toastLoading(true)
        setTrust(if (spam) Trust.blacklist else Trust.whitelist)
        spamView.visibility = View.GONE
        headerView.setSubtitle(null)
        viewModel.reportSpam(spam) {
            navigation?.toastLoading(false)
            if (spam) {
                navigation?.toast(Localization.nft_marked_as_spam)
                finish()
            }
        }
    }

    private fun hideCollection() {
        viewModel.hideCollection { finish() }
    }

    private fun openTonViewer() {
        navigation?.openURL("https://tonviewer.com/${nftEntity.address}")
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
            val nftExplorer = context?.remoteConfig?.nftExplorer ?: return@setOnClickListener
            val explorerUrl = nftExplorer.format(address)
            navigation?.openURL(explorerUrl)
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

        private const val HIDE_NFT_ID = 1L
        private const val HIDE_AND_REPORT_ID = 2L
        private const val VIEWER_ID = 3L
        private const val BURN_ID = 4L

        private const val ARG_ENTITY = "entity"

        fun newInstance(wallet: WalletEntity, entity: NftEntity): NftScreen {
            val fragment = NftScreen(wallet)
            fragment.putParcelableArg(ARG_ENTITY, entity)
            return fragment
        }
    }
}