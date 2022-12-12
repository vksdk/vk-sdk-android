package com.petersamokhin.vksdk.android.auth.hidden

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

internal class HiddenFragment : Fragment() {
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (!alreadyStarted()) {
            startActivityForResult(
                intent(),
                requestCode()
            )
            requireArguments().putBoolean(EXTRA_ALREADY_STARTED, true)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        parentFragmentManager.setFragmentResult(requestCode.toString(), data?.extras ?: Bundle())
    }

    private fun alreadyStarted(): Boolean = requireArguments().getBoolean(EXTRA_ALREADY_STARTED)

    @CheckResult
    private fun intent(): Intent = requireArguments().getParcelable(EXTRA_INTENT)!!

    @CheckResult
    private fun requestCode(): Int = requireArguments().getInt(EXTRA_REQUEST_CODE)

    companion object {

        @CheckResult
        @JvmStatic
        fun newInstance(
            launchIntent: Intent,
            requestCode: Int,
        ): HiddenFragment {
            return HiddenFragment().apply {
                arguments = bundleOf(
                    EXTRA_INTENT to launchIntent,
                    EXTRA_REQUEST_CODE to requestCode
                )
            }
        }

        @JvmStatic
        fun clear() {
        }

        private const val EXTRA_INTENT = "EXTRA_INTENT"
        private const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        private const val EXTRA_ALREADY_STARTED = "EXTRA_ALREADY_STARTED"
    }
}