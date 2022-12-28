package com.petersamokhin.vksdk.android.auth.hidden

import android.content.Context
import android.content.Intent
import androidx.annotation.CheckResult
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

internal class HiddenFragment : Fragment() {
    @Suppress("DEPRECATION")
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode()) {
            lastListener?.onActivityResult(requestCode, resultCode, data)
            lastListener = null
        }
    }

    private fun alreadyStarted(): Boolean = requireArguments().getBoolean(EXTRA_ALREADY_STARTED)

    @Suppress("DEPRECATION")
    @CheckResult
    private fun intent(): Intent = requireArguments().getParcelable(EXTRA_INTENT)!!

    @CheckResult
    private fun requestCode(): Int = requireArguments().getInt(EXTRA_REQUEST_CODE)

    companion object {
        private var lastListener: ActivityResultListener? = null

        @CheckResult
        @JvmStatic
        fun newInstance(
            launchIntent: Intent,
            requestCode: Int,
            listener: ActivityResultListener
        ): HiddenFragment {
            if (lastListener != null) throw IllegalStateException()
            lastListener = listener
            return HiddenFragment().apply {
                arguments = bundleOf(
                    EXTRA_INTENT to launchIntent,
                    EXTRA_REQUEST_CODE to requestCode
                )
            }
        }

        @JvmStatic
        fun clear() {
            lastListener = null
        }

        private const val EXTRA_INTENT = "EXTRA_INTENT"
        private const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"
        private const val EXTRA_ALREADY_STARTED = "EXTRA_ALREADY_STARTED"
    }
}

internal interface ActivityResultListener {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}