package net.k1ra.succubotapp.features.appSettings.adapters

import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.ManageUserCardBinding
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.adapters.BaseRecyclerViewAdapter

class UserListAdapter(
    val list: ArrayList<User>,
    private val actionListener: Listener
) : BaseRecyclerViewAdapter<ManageUserCardBinding, User>(list) {

    interface Listener {
        fun onClicked(item: User)
    }

    override val layoutId: Int = R.layout.manage_user_card

    override fun bind(binding: ManageUserCardBinding, item: User) {
        binding.apply {
            user = item
            listener = actionListener
            executePendingBindings()
        }
    }
}