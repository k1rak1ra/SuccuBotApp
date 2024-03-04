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
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.CurrentServerConfiguration
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.ServerManagementRequest
import net.k1ra.succubotapp.features.appSettings.model.serverManagement.ServerManagementResponse
import net.k1ra.succubotapp.features.appSettings.repository.ServerManagementRetrofitInterface
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") //It's a Hilt ApplicationContext, stop complaining...
class ServerManagementViewModel @Inject constructor(
    retrofit: Retrofit,
    @ApplicationContext val context: Context
) : ViewModel() {
    private val client = retrofit.create(ServerManagementRetrofitInterface::class.java)

    val currentServerConfigurationStatus = MutableLiveData<LoadingStatus<CurrentServerConfiguration>>()
    val updateServerSettingStatus = MutableLiveData<LoadingStatus<ServerManagementResponse>>()

    fun getCurrentServerConfiguration() = CoroutineScope(Dispatchers.IO).launch {
        client.getCurrentServerConfiguration().enqueue(BaseRetrofitCallback(currentServerConfigurationStatus::setValue))
    }

    fun updateServerSetting(request: ServerManagementRequest) = CoroutineScope(Dispatchers.IO).launch {
        client.updateServerSetting(request).enqueue(BaseRetrofitCallback(updateServerSettingStatus::setValue))
    }
}