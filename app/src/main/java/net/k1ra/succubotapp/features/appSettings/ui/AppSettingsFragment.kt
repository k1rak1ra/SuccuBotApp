package net.k1ra.succubotapp.features.appSettings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentAppSettingsBinding
import net.k1ra.succubotapp.features.appSettings.model.UserRequest
import net.k1ra.succubotapp.features.appSettings.viewmodel.UserSettingsViewModel
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import javax.inject.Inject

@AndroidEntryPoint
class AppSettingsFragment : Fragment() {
    private lateinit var binding: FragmentAppSettingsBinding
    private val viewModel: UserSettingsViewModel by viewModels()
    var user: User? = null
        @Inject set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = resources.getString(R.string.settings)

        binding.settingsAccountCard.setOnClickListener {
            findNavController().navigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToAccountSettingsFragment())
        }

        binding.settingsAboutCard.setOnClickListener {
            findNavController().navigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToAboutSettingsFragment())
        }

        if (!user!!.admin) {
            binding.settingsManageUsersCard.visibility = View.GONE
            binding.settingsManageServerCard.visibility = View.GONE
            binding.settingsManageDevicesCard.visibility = View.GONE
        }

        binding.settingsManageUsersCard.setOnClickListener {
            findNavController().navigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToSettingsManageUsersFragment())
        }

        binding.settingsManageServerCard.setOnClickListener {
            findNavController().navigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToSettingsManageServerFragment())
        }

        binding.settingsManageDevicesCard.setOnClickListener {
            findNavController().navigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToSettingsManageDevicesFragment())
        }

        binding.settingsLogoutCard.setOnClickListener {
            viewModel.updateUserStatus.value = Loading(resources.getString(R.string.logging_out))
            viewModel.updateUser(UserRequest(logout = true))
        }

        viewModel.updateUserStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            when (it) {
                is Failure -> requireContext().sendBroadcast(Constants.LogoutIntent)
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> requireContext().sendBroadcast(Constants.LogoutIntent)
            }
        }
    }
}