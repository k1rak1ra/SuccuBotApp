package net.k1ra.succubotapp.features.dashboard.adapters

import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.DashboardCardBinding
import net.k1ra.succubotapp.features.base.adapters.BaseRecyclerViewAdapter
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus

class DashboardAdapter(
    val list: ArrayList<RobotStatus>,
    private val actionListener: Listener
) : BaseRecyclerViewAdapter<DashboardCardBinding, RobotStatus>(list) {

    interface Listener {
        fun onClicked(item: RobotStatus)
    }

    override val layoutId: Int = R.layout.dashboard_card

    override fun bind(binding: DashboardCardBinding, item: RobotStatus) {
        binding.apply {
            robot = item
            listener = actionListener
            executePendingBindings()
        }
    }
}