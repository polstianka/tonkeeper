package com.tonapps.tonkeeper.ui.screen.phrase

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.tonapps.tonkeeper.extensions.copyToClipboard
import com.tonapps.tonkeeperx.R
import uikit.base.BaseFragment
import uikit.extensions.pinToBottomInsets
import uikit.widget.HeaderView
import uikit.widget.PhraseWords

class PhraseScreen: BaseFragment(R.layout.fragment_phrase), BaseFragment.SwipeBack {

    private val words: Array<String> by lazy {
        requireArguments().getStringArray(ARG_WORDS)!!
    }

    private lateinit var headerView: HeaderView
    private lateinit var wordsView: PhraseWords
    private lateinit var copyButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        headerView = view.findViewById(R.id.header)
        headerView.doOnCloseClick = { finish() }

        wordsView = view.findViewById(R.id.words)
        wordsView.setWords(words)

        copyButton = view.findViewById(R.id.copy)
        copyButton.pinToBottomInsets()
        copyButton.setOnClickListener {
            requireContext().copyToClipboard(words.joinToString(" "))
        }
    }

    companion  object {
        private const val ARG_WORDS = "words"

        fun newInstance(words: Array<String>): PhraseScreen {
            val fragment = PhraseScreen()
            fragment.arguments = Bundle().apply {
                putStringArray(ARG_WORDS, words)
            }
            return fragment
        }
    }
}