package net.k1ra.succubotapp.features.base.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.ErrorOverlayBinding

class ErrorOverlay : DialogFragment() {
    private lateinit var binding: ErrorOverlayBinding
    private var text: String? = ""
    private var action: Runnable? = Runnable { }

    companion object {
        private const val TAG = "ErrorOverlay"
        private var instance: ErrorOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): ErrorOverlay {
            if (instance == null) {
                instance = ErrorOverlay()
            }
            return instance!!
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ErrorOverlayBinding.inflate(layoutInflater)
        isCancelable = false
        dialog?.window?.setBackgroundDrawable(ColorDrawable(requireContext().getColor(R.color.background)))

        //Workaround to allow pressing back on the error overlay to go back to the previous fragment in the back stack
        dialog!!.setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
                true
            } else false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTextAndAction()
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Show overlay with specified text and action
     */
    fun showOverlay(frgManager: FragmentManager, text: String?, action: Runnable?) {
        this.text = text
        this.action = action
        if (isResumed) {
            updateTextAndAction()
            return
        } else {
            hide()
            show(frgManager, TAG)
        }
    }

    /**
     * Hide the overlay
     */
    fun hide() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            //Ignore
        }
    }

    private fun updateTextAndAction() {
        binding.errorOverlayText.text = text
        binding.errorOverlayTryAgain.setOnClickListener { action?.run() }
    }
}