package net.k1ra.succubotapp.features.appSettings.adapters

import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.ManageDeviceCardBinding
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.RobotInfo
import net.k1ra.succubotapp.features.base.adapters.BaseRecyclerViewAdapter

class DeviceListAdapter(
    val list: ArrayList<RobotInfo>,
    private val actionListener: Listener
) : BaseRecyclerViewAdapter<ManageDeviceCardBinding, RobotInfo>(list) {

    interface Listener {
        fun onClicked(item: RobotInfo)
    }

    override val layoutId: Int = R.layout.manage_device_card

    override fun bind(binding: ManageDeviceCardBinding, item: RobotInfo) {
        binding.apply {
            robot = item
            listener = actionListener
            executePendingBindings()
        }
    }
}