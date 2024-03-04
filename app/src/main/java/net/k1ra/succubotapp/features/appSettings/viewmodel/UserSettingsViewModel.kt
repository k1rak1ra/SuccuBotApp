package net.k1ra.succubotapp.features.appSettings.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.di.EncryptedSharedPrefProvider
import net.k1ra.succubotapp.features.appSettings.model.UserRequest
import net.k1ra.succubotapp.features.appSettings.repository.UserSettingsRetrofitInterface
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") //It's a Hilt ApplicationContext, stop complaining...
class UserSettingsViewModel @Inject constructor(
    private val sharedpref: EncryptedSharedPrefProvider,
    retrofit: Retrofit,
    private val gson: Gson,
    @ApplicationContext val context: Context
) : ViewModel() {
    private val client = retrofit.create(UserSettingsRetrofitInterface::class.java)

    val updateUserStatus = MutableLiveData<LoadingStatus<User>>()

    fun updateUser(request: UserRequest) = CoroutineScope(Dispatchers.IO).launch {
        client.sendUserRequest(request).enqueue(BaseRetrofitCallback(updateUserStatus::setValue) {
            if (request.logoutOutOfAll == true) {
                context.sendBroadcast(Constants.LogoutIntent)
            }

            sharedpref.setStored(Constants.CurrentUserName, gson.toJson(it))
        })
    }
}