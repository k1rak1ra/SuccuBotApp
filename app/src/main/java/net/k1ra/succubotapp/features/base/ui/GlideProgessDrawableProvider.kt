package net.k1ra.succubotapp.features.base.ui

import android.content.Context
import android.graphics.drawable.Drawable

object GlideProgessDrawableProvider {
    fun getProgressBarIndeterminate(context: Context): Drawable? {
        val attrs = intArrayOf(android.R.attr.indeterminateDrawable)
        val intermediateDrawableIndex = 0
        val a = context.obtainStyledAttributes(android.R.style.Widget_Material_ProgressBar, attrs)
        return try {
            a.getDrawable(intermediateDrawableIndex)
        } finally {
            a.recycle()
        }
    }
}