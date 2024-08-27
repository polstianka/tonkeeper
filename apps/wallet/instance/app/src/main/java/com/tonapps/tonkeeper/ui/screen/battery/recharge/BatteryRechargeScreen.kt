package com.tonapps.tonkeeper.ui.screen.battery.recharge

import android.os.Bundle
import com.tonapps.extensions.getParcelableCompat
import com.tonapps.tonkeeperx.R
import com.tonapps.wallet.api.entity.TokenEntity
import uikit.base.BaseFragment

class BatteryRechargeScreen: BaseFragment(R.layout.fragment_battery_recharge), BaseFragment.BottomSheet {

    private val token: TokenEntity by lazy { requireArguments().getParcelableCompat(ARG_TOKEN)!! }

    companion object {

        private const val ARG_TOKEN = "token"

        fun newInstance(token: TokenEntity): BatteryRechargeScreen {
            val fragment = BatteryRechargeScreen()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_TOKEN, token)
            }
            return fragment
        }
    }
}