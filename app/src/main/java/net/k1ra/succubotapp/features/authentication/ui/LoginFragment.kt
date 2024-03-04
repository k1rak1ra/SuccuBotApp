package net.k1ra.succubotapp.features.authentication.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentLoginBinding
import net.k1ra.succubotapp.features.authentication.viewmodel.LoginViewModel
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Do nothing, prevent navigation back
            }
        })

        requireActivity().title = getString(R.string.app_name)

        if (viewModel.currentUser != null)
            navigateToDashboardFragment()

        binding.btnLogin.setOnClickListener {
            viewModel.loginStatus.value = Loading(resources.getString(R.string.logging_you_in))
            viewModel.login(
                binding.fieldUsername.editText!!.text.toString(),
                binding.fieldPassword.editText!!.text.toString()
            )
        }

        viewModel.loginStatus.observeForever {
            when(it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code == 403) {
                        binding.fieldPassword.error = resources.getString(R.string.incorrect_credentials)
                        binding.fieldUsername.error = resources.getString(R.string.incorrect_credentials)
                    } else {
                        binding.fieldPassword.error = resources.getString(R.string.connection_failed)
                        binding.fieldUsername.error = resources.getString(R.string.connection_failed)
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> navigateToDashboardFragment()
            }
        }

        binding.btnLoginSettings.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToAuthenticationSettingsFragment())
        }
    }

    private fun navigateToDashboardFragment() {
        LoadingOverlay.getInstance().hide()

        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToDashboardFragment())
    }
}