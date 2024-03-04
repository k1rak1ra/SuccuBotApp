package net.k1ra.succubotapp.features.appSettings.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.DeviceManagementRequest
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.DeviceManagementResponse
import net.k1ra.succubotapp.features.appSettings.model.deviceManagement.RobotInfo
import net.k1ra.succubotapp.features.appSettings.repository.DeviceManagementRetrofitInterface
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") //It's a Hilt ApplicationContext, stop complaining...
class DeviceManagementViewModel @Inject constructor(
    retrofit: Retrofit,
    @ApplicationContext val context: Context
) : ViewModel() {
    private val client = retrofit.create(DeviceManagementRetrofitInterface::class.java)

    val currentDevicesStatus = MutableLiveData<LoadingStatus<List<RobotInfo>>>()
    val updateDeviceKeysAndTestStatus = MutableLiveData<LoadingStatus<DeviceManagementResponse>>()

    fun getCurrentDevices() = CoroutineScope(Dispatchers.IO).launch {
        client.getCurrentDevices().enqueue(BaseRetrofitCallback(currentDevicesStatus::setValue))
    }

    fun updateDeviceKeysAndTest(request: DeviceManagementRequest) = CoroutineScope(Dispatchers.IO).launch {
        client.updateDeviceKeysAndTest(request).enqueue(BaseRetrofitCallback(updateDeviceKeysAndTestStatus::setValue))
    }
}