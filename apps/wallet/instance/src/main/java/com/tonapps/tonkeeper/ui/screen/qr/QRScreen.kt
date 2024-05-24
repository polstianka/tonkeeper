package com.tonapps.tonkeeper.ui.screen.qr

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.tonapps.qr.ui.QRView
import com.tonapps.tonkeeper.extensions.copyToClipboard
import com.tonapps.tonkeeper.extensions.toast
import com.tonapps.tonkeeperx.R
import com.tonapps.uikit.color.accentOrangeColor
import com.tonapps.uikit.color.accentPurpleColor
import com.tonapps.uikit.color.backgroundContentTintColor
import com.tonapps.uikit.color.stateList
import com.tonapps.wallet.api.entity.TokenEntity
import com.tonapps.wallet.data.account.WalletType
import com.tonapps.wallet.localization.Localization
import uikit.base.BaseFragment
import uikit.navigation.Navigation.Companion.navigation
import uikit.widget.FrescoView
import uikit.widget.HeaderView
import uikit.widget.TextHeaderView

class QRScreen: BaseFragment(R.layout.fragment_qr), BaseFragment.BottomSheet {

    private val args: QRArgs by lazy {
        QRArgs(requireArguments())
    }

    private lateinit var headerView: HeaderView
    private lateinit var infoView: TextHeaderView
    private lateinit var qrView: QRView
    private lateinit var iconView: FrescoView
    private lateinit var addressView: AppCompatTextView
    private lateinit var walletTypeView: AppCompatTextView
    private lateinit var copyView: View
    private lateinit var shareView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        infoView = view.findViewById(R.id.info)
        infoView.title = getString(Localization.receive_coin, args.token.name)
        infoView.desciption = getString(Localization.receive_coin_description, args.token.name)

        qrView = view.findViewById(R.id.qr)
        qrView.setContent(args.getDeepLink())

        iconView = view.findViewById(R.id.icon)
        iconView.setImageURI(args.token.imageUri)

        addressView = view.findViewById(R.id.address)
        addressView.setOnClickListener { copy() }
        addressView.text = args.address

        walletTypeView = view.findViewById(R.id.wallet_type)
        if (args.walletType == WalletType.Default) {
            walletTypeView.visibility = View.GONE
        } else {
            walletTypeView.visibility = View.VISIBLE
            if (args.walletType == WalletType.Watch) {
                walletTypeView.setText(Localization.watch_only)
                walletTypeView.backgroundTintList = requireContext().accentPurpleColor.stateList
            } else if (args.walletType == WalletType.Testnet) {
                walletTypeView.setText(Localization.testnet)
                walletTypeView.backgroundTintList = requireContext().accentPurpleColor.stateList
            } else if (args.walletType == WalletType.Signer) {
                walletTypeView.setText(Localization.signer)
                walletTypeView.backgroundTintList = requireContext().accentPurpleColor.stateList
            }
        }

        copyView = view.findViewById(R.id.copy)
        copyView.setOnClickListener { copy() }

        shareView = view.findViewById(R.id.share)
        shareView.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, args.address)
            startActivity(Intent.createChooser(intent, getString(Localization.share)))
        }
    }

    private fun copy() {
        val color = when (args.walletType) {
            WalletType.Default -> requireContext().backgroundContentTintColor
            WalletType.Signer -> requireContext().accentPurpleColor
            else -> requireContext().accentOrangeColor
        }
        navigation?.toast(getString(Localization.copied), color)
        context?.copyToClipboard(args.address)
    }

    companion object {

        fun newInstance(address: String, token: TokenEntity, walletType: WalletType): QRScreen {
            val screen = QRScreen()
            screen.setArgs(QRArgs(address, token, walletType))
            return screen
        }
    }
}