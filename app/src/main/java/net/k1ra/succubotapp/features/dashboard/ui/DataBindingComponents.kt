package net.k1ra.succubotapp.features.dashboard.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import net.k1ra.succubotapp.features.robotStatus.ui.BatteryGauge
import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZoneOffset

@BindingAdapter("bind:lastCleanTime")
fun bindLastCleanTime(tv: TextView, robot: RobotStatus) {
    tv.text = tv.context.getString(
        R.string.last_cleaned,
        PrettyTime().format(robot.lastCleaned.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
    )
}

@BindingAdapter("bind:robotStatus")
fun bindRobotStatus(tv: TextView, robot: RobotStatus) {
    tv.text = when(robot.status) {
        "standby" -> tv.context.getString(R.string.standby)
        "charging" -> tv.context.getString(R.string.charging)
        "relocating" -> tv.context.getString(R.string.relocating)
        "goto_charge" -> tv.context.getString(R.string.goto_charge)
        "sleep" -> tv.context.getString(R.string.sleep)
        "smart" -> tv.context.getString(R.string.smart)
        else -> tv.context.getString(R.string.unknown_status)
    }
}

@BindingAdapter("bind:gaugeValue")
fun bindGaugeValue(gauge: BatteryGauge, robot: RobotStatus) {
    gauge.value = robot.battery
}