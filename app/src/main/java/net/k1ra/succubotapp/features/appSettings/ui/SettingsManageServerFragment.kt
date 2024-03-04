package net.k1ra.succubotapp.features.appSettings.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentSettingsManageServerBinding
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.ServerManagementRequest
import net.k1ra.succubotapp.features.appSettings.viewmodel.ServerManagementViewModel
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.ErrorOverlay
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay

@AndroidEntryPoint
class SettingsManageServerFragment : Fragment() {
    private lateinit var binding: FragmentSettingsManageServerBinding
    private val viewModel: ServerManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsManageServerBinding.inflate(inflater)
        return binding.root
    }

    private val mqttCertChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { certUri ->
        certUri ?: return@registerForActivityResult

        requireContext().contentResolver.openInputStream(certUri)?.readBytes()?.decodeToString().let { certString ->
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_mqtt_connection_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeMqttCertificate = certString
            ))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentServerConfigurationStatus.value = Loading(getString(R.string.loading_server_settings))
        viewModel.getCurrentServerConfiguration()
        viewModel.currentServerConfigurationStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            ErrorOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code != 401) {
                        ErrorOverlay.getInstance().showOverlay(parentFragmentManager, resources.getString(
                                R.string.connection_failed
                            )) {
                            viewModel.currentServerConfigurationStatus.value = Loading(getString(R.string.loading_server_settings))
                            viewModel.getCurrentServerConfiguration()
                        }
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    binding.fieldMinPasswordLength.editText?.setText(it.result.minPasswordLength.toString())
                    binding.fieldMaxImageSize.editText?.setText(it.result.maxImageSize.toString())
                    binding.fieldCurrentBaseUrl.editText?.setText(it.result.currentBaseUrl)
                    binding.fieldLdapLoginEnabled.isChecked = it.result.ldapLoginEnabled

                    binding.fieldLdapServer.editText?.setText(it.result.ldapServer)
                    binding.fieldLdapBindUser.editText?.setText(it.result.ldapBindUser)
                    binding.fieldLdapBindPassword.editText?.setText(it.result.ldapBindPassword)
                    binding.fieldLdapTlsEnabled.isChecked = it.result.ldapTlsEnabled

                    binding.fieldLdapUserDn.editText?.setText(it.result.ldapUserDn)
                    binding.fieldLdapUserFilter.editText?.setText(it.result.ldapUserFilter)
                    binding.fieldLdapUserUidAttribute.editText?.setText(it.result.ldapUserUidAttribute)

                    binding.fieldLdapAdminGroupName.editText?.setText(it.result.ldapAdminGroupName)
                    binding.fieldLdapGroupDn.editText?.setText(it.result.ldapGroupDn)
                    binding.fieldLdapGroupFilter.editText?.setText(it.result.ldapGroupFilter)

                    binding.fieldMqttServer.editText?.setText(it.result.mqttServerUrl)
                    setMqttStatus(it.result.mqttIsConnected, it.result.mqttError)
                }
            }
        }

        viewModel.updateServerSettingStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            when (it) {
                is Failure -> Snackbar.make(view, R.string.connection_failed, Snackbar.LENGTH_SHORT).show()
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    if (it.result.ldapUserTestResponse != null) {
                        val message = StringBuilder()
                        if (it.result.ldapUserTestResponse?.error != null)
                            message.append(it.result.ldapUserTestResponse!!.error)
                        it.result.ldapUserTestResponse?.elements?.forEach { line -> message.append("$line\n") }

                        AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.ldap_test_result))
                            .setMessage(message)
                            .setPositiveButton(getString(R.string.dismiss)) { d, _ -> d.dismiss() }
                            .show()
                    }

                    if (it.result.ldapGroupTestResponse != null) {
                        val message = StringBuilder()
                        if (it.result.ldapGroupTestResponse?.error != null)
                            message.append(it.result.ldapGroupTestResponse!!.error)

                        if (it.result.ldapGroupTestResponse!!.groups.isNotEmpty()) {
                            message.append(getString(R.string.user_groups))
                            message.append("\n")
                            it.result.ldapGroupTestResponse!!.groups.forEach { group -> message.append("$group\n") }

                            if (it.result.ldapGroupTestResponse!!.isAdmin)
                                message.append("\n\n${getString(R.string.ldap_is_admin)}")
                        }

                        AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.ldap_test_result))
                            .setMessage(message)
                            .setPositiveButton(getString(R.string.dismiss)) { d, _ -> d.dismiss() }
                            .show()
                    }

                    setMqttStatus(it.result.mqttIsConnected, it.result.mqttError)

                    if (it.result.ldapGroupTestResponse == null && it.result.ldapUserTestResponse == null)
                        Snackbar.make(view, R.string.updated_successfully, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnSaveGeneralServerSettings.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_server_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeMinPasswordLength = Integer.parseInt(binding.fieldMinPasswordLength.editText!!.text.toString()),
                changeMaxImageSize = Integer.parseInt(binding.fieldMaxImageSize.editText!!.text.toString()),
                changeCurrentBaseUrl = binding.fieldCurrentBaseUrl.editText!!.text.toString(),
                changeLdapLoginEnabled = binding.fieldLdapLoginEnabled.isChecked
            ))
        }

        binding.btnSaveMqttServerSettings.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_mqtt_connection_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeMqttServer = binding.fieldMqttServer.editText!!.text.toString()
            ))
        }

        binding.btnUpdateMqttCertificate.setOnClickListener {
            mqttCertChooser.launch("application/x-x509-ca-cert")
        }

        binding.btnSaveLdapServerSettings.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_ldap_connection_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeLdapServer = binding.fieldLdapServer.editText!!.text.toString(),
                changeLdapBindUser = binding.fieldLdapBindUser.editText!!.text.toString(),
                changeLdapBindPassword = binding.fieldLdapBindPassword.editText!!.text.toString(),
                changeLdapTlsEnabled = binding.fieldLdapTlsEnabled.isChecked
            ))
        }

        binding.btnSaveLdapUserSettings.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_ldap_user_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeLdapUserDn = binding.fieldLdapUserDn.editText!!.text.toString(),
                changeLdapUserFilter = binding.fieldLdapUserFilter.editText!!.text.toString(),
                changeLdapUserUidAttribute = binding.fieldLdapUserUidAttribute.editText!!.text.toString()
            ))
        }

        binding.btnTestLdapUser.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.testing_ldap))
            viewModel.updateServerSetting(ServerManagementRequest(runLdapUserTestOnUser = binding.fieldLdapUserTest.editText!!.text.toString()))
        }

        binding.btnSaveLdapGroupSettings.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.saving_ldap_group_settings))
            viewModel.updateServerSetting(ServerManagementRequest(
                changeLdapGroupDn = binding.fieldLdapGroupDn.editText!!.text.toString(),
                changeLdapGroupFilter = binding.fieldLdapGroupFilter.editText!!.text.toString(),
                changeLdapAdminGroupName = binding.fieldLdapAdminGroupName.editText!!.text.toString()
            ))
        }

        binding.btnTestLdapGroup.setOnClickListener {
            viewModel.updateServerSettingStatus.value = Loading(getString(R.string.testing_ldap))
            viewModel.updateServerSetting(ServerManagementRequest(runLdapGroupTestOnUser = binding.fieldLdapGroupTest.editText!!.text.toString()))
        }
    }

    private fun setMqttStatus(connected: Boolean, error: String?) {
        if (connected) {
            binding.mqttStatusIcon.setImageResource(R.drawable.checkmark)
            binding.mqttStatusText.text = getString(R.string.mqtt_connected)
        } else {
            binding.mqttStatusIcon.setImageResource(R.drawable.error)
            binding.mqttStatusText.text = getString(R.string.mqtt_failed_to_connect, error ?: "")
        }
    }
}