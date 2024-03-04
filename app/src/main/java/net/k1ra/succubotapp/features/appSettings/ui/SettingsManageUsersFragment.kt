package net.k1ra.succubotapp.features.appSettings.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentSettingsManageUsersBinding
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.appSettings.adapters.UserListAdapter
import net.k1ra.succubotapp.features.appSettings.viewmodel.UserManagementViewModel
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.ErrorOverlay
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import net.k1ra.succubotapp.features.dashboard.ui.DashboardFragmentDirections
import net.k1ra.succubotapp.utils.SelfReference
import net.k1ra.succubotapp.utils.selfReference
import javax.inject.Inject

@AndroidEntryPoint
class SettingsManageUsersFragment : Fragment() {
    private lateinit var binding: FragmentSettingsManageUsersBinding
    private val viewModel: UserManagementViewModel by viewModels()
    var selectedUserIndex: Int? = null
    var user: User? = null
        @Inject set

    private val adapter: UserListAdapter = selfReference {
        UserListAdapter(arrayListOf(), object : UserListAdapter.Listener {
            override fun onClicked(item: User) {
                if (user?.uid == item.uid) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.error))
                        .setMessage(getString(R.string.user_is_you))
                        .setPositiveButton(getString(R.string.dismiss)) { d, _ -> d.dismiss() }
                        .show()
                } else {
                    selectedUserIndex = self.list.indexOf(item)
                    findNavController().navigate(
                        SettingsManageUsersFragmentDirections.actionSettingsManageUsersFragmentToUserEditFragment(
                            GsonProvider.provideGson().toJson(item)
                        )
                    )
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsManageUsersBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageUsersRv.layoutManager = LinearLayoutManager(requireContext())
        binding.manageUsersRv.adapter = adapter

        viewModel.currentUsersStatus.value = Loading(getString(R.string.loading_users))
        viewModel.getCurrentUsers()
        viewModel.currentUsersStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            ErrorOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code != 401) {
                        ErrorOverlay.getInstance().showOverlay(parentFragmentManager, resources.getString(
                            R.string.connection_failed
                        )) {
                            viewModel.currentUsersStatus.value = Loading(getString(R.string.loading_users))
                            viewModel.getCurrentUsers()
                        }
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    adapter.list.clear()
                    adapter.list.addAll(it.result)
                    adapter.notifyDataSetChanged()
                }
            }
        }

        binding.btnNewUser.setOnClickListener {
            selectedUserIndex = null
            findNavController().navigate(
                SettingsManageUsersFragmentDirections.actionSettingsManageUsersFragmentToUserEditFragment(null)
            )
        }
    }
}