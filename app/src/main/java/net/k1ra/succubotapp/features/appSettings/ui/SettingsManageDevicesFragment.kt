package net.k1ra.succubotapp.features.appSettings.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentSettingsManageDevicesBinding
import net.k1ra.succubotapp.features.appSettings.adapters.DeviceListAdapter
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.DeviceManagementRequest
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.RobotInfo
import net.k1ra.succubotapp.features.appSettings.viewmodel.DeviceManagementViewModel
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.ErrorOverlay
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import java.lang.StringBuilder

@AndroidEntryPoint
class SettingsManageDevicesFragment : Fragment() {
    private lateinit var binding: FragmentSettingsManageDevicesBinding
    private val viewModel: DeviceManagementViewModel by viewModels()

    private val adapter = DeviceListAdapter(arrayListOf(), object: DeviceListAdapter.Listener {
        override fun onClicked(item: RobotInfo) {
            viewModel.updateDeviceKeysAndTestStatus.value = Loading(getString(R.string.saving_and_testing))
            viewModel.updateDeviceKeysAndTest(DeviceManagementRequest(item.did, item.webKey, item.mqttKey))
        }
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsManageDevicesBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageDevicesRv.layoutManager = LinearLayoutManager(requireContext())
        binding.manageDevicesRv.adapter = adapter

        viewModel.currentDevicesStatus.value = Loading(getString(R.string.loading_devices))
        viewModel.getCurrentDevices()
        viewModel.currentDevicesStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            ErrorOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code != 401) {
                        ErrorOverlay.getInstance().showOverlay(parentFragmentManager, resources.getString(
                            R.string.connection_failed
                        )) {
                            viewModel.currentDevicesStatus.value = Loading(getString(R.string.loading_devices))
                            viewModel.getCurrentDevices()
                        }
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    adapter.list.clear()

                    if (it.result.isEmpty()) {
                        binding.manageDevicesNoneFound.visibility = View.VISIBLE
                        binding.manageDevicesRv.visibility = View.GONE
                    } else {
                        binding.manageDevicesNoneFound.visibility = View.GONE
                        binding.manageDevicesRv.visibility = View.VISIBLE

                        adapter.list.addAll(it.result)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        viewModel.updateDeviceKeysAndTestStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            ErrorOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code != 401) {
                        ErrorOverlay.getInstance().showOverlay(parentFragmentManager, resources.getString(
                            R.string.connection_failed
                        )) {
                            ErrorOverlay.getInstance().hide()
                        }
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    val message = StringBuilder()

                    if (it.result.mqttKeyValid)
                        message.append(getString(R.string.mqtt_key_valid))
                    else
                        message.append(getString(R.string.mqtt_key_invalid))

                    message.append("\n")

                    if (it.result.webKeyValid == true)
                        message.append(getString(R.string.web_key_valid))
                    else if (it.result.webKeyValid == false)
                        message.append(getString(R.string.web_key_invalid))

                    message.append("\n")

                    if (it.result.mqttKeyValid && it.result.webKeyValid == true)
                        message.append(getString(R.string.restart_robot))

                    AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.robot_key_test_result))
                        .setMessage(message)
                        .setPositiveButton(getString(R.string.dismiss)) { d, _ -> d.dismiss() }
                        .show()
                }
            }
        }
    }
}