package net.k1ra.succubotapp.features.appSettings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.BuildConfig
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentSettingsAboutBinding

@AndroidEntryPoint
class AboutSettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsAboutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = resources.getString(R.string.settings_about)

        binding.appVersionString.text = resources.getString(R.string.version, BuildConfig.VERSION_NAME)
    }
}