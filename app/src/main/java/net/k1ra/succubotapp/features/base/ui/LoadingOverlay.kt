package net.k1ra.succubotapp.features.base.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.LoadingOverlayBinding

class LoadingOverlay : DialogFragment() {
    private lateinit var binding: LoadingOverlayBinding
    private var text: String? = ""

    companion object {
        private const val TAG = "LoadingOverlay"
        private var instance: LoadingOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): LoadingOverlay {
            if (instance == null) {
                instance = LoadingOverlay()
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
        binding = LoadingOverlayBinding.inflate(layoutInflater)
        isCancelable = false
        dialog?.window?.setBackgroundDrawable(ColorDrawable(requireContext().getColor(R.color.background)))
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
     * Show overlay with specified text
     */
    fun showOverlay(frgManager: FragmentManager, text: String?) {
        this.text = text
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
        binding.loadingOverlayText.text = text
    }
}