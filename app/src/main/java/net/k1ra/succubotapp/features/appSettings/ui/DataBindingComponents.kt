package net.k1ra.succubotapp.features.appSettings.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.RobotInfo
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.ui.GlideProgessDrawableProvider
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import net.k1ra.succubotapp.features.robotStatus.ui.BatteryGauge
import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZoneOffset

@BindingAdapter("bind:lastPingedTime")
fun bindLastPingedTime(tv: TextView, robot: RobotInfo) {
    tv.text = tv.context.getString(
        R.string.last_seen,
        PrettyTime().format(robot.lastPingAt.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
    )
}

@BindingAdapter("bind:imageWithGlide")
fun bindImageWithGlide(iv: ImageView, url: String) {
    Glide
        .with(iv.context)
        .load(url)
        .circleCrop()
        .placeholder(GlideProgessDrawableProvider.getProgressBarIndeterminate(iv.context))
        .fallback(R.drawable.user)
        .into(iv)
}

@BindingAdapter("bind:userType")
fun bindUserType(tv: TextView, user: User) {
    tv.text = if (user.native) {
      tv.context.getString(R.string.native_user)
    } else {
        tv.context.getString(R.string.ldap_user)
    }
}

@BindingAdapter("bind:userIsAdmin")
fun bindUserIsAdmin(tv: TextView, user: User) {
    tv.text = if (user.admin) {
        tv.context.getString(R.string.ldap_is_admin)
    } else {
        ""
    }
}
