package net.k1ra.succubotapp.features.authentication.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentAuthenticationSettingsBinding
import net.k1ra.succubotapp.di.EncryptedSharedPrefProvider
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.authentication.model.AuthenticationSettings
import javax.inject.Inject


@AndroidEntryPoint
class AuthenticationSettingsFragment : Fragment() {
    private lateinit var binding: FragmentAuthenticationSettingsBinding
    var settings: AuthenticationSettings? = null
        @Inject set
    var sharedPref: EncryptedSharedPrefProvider? = null
        @Inject set

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticationSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fieldServerEndpoint.editText?.setText(settings!!.baseUrl)

        binding.btnSaveServerEndpoint.setOnClickListener {
            settings!!.baseUrl = binding.fieldServerEndpoint.editText!!.text.toString()

            sharedPref!!.setStored(Constants.AuthenticationSettingsName, GsonProvider.provideGson().toJson(settings))
            Snackbar.make(view, R.string.updated_successfully, Snackbar.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                triggerRebirth(requireContext())
            }
        })
    }

    private fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        mainIntent.setPackage(context.packageName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }
}