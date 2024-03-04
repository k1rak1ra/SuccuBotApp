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
import net.k1ra.succubotapp.features.appSettings.model.userManagement.UserManagementRequest
import net.k1ra.succubotapp.features.appSettings.repository.UserManagementRetrofitInterface
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak") //It's a Hilt ApplicationContext, stop complaining...
class UserManagementViewModel @Inject constructor(
    retrofit: Retrofit,
    @ApplicationContext val context: Context
) : ViewModel() {
    private val client = retrofit.create(UserManagementRetrofitInterface::class.java)

    val currentUsersStatus = MutableLiveData<LoadingStatus<List<User>>>()
    val updateUserStatus = MutableLiveData<LoadingStatus<User>>()
    val deleteUserStatus = MutableLiveData<LoadingStatus<Unit>>()

    fun getCurrentUsers() = CoroutineScope(Dispatchers.IO).launch {
        client.getCurrentUsers().enqueue(BaseRetrofitCallback(currentUsersStatus::setValue))
    }

    fun updateUser(uid: String, request: UserManagementRequest) = CoroutineScope(Dispatchers.IO).launch {
        client.updateUser(uid, request).enqueue(BaseRetrofitCallback(updateUserStatus::setValue))
    }

    fun deleteUser(uid: String) = CoroutineScope(Dispatchers.IO).launch {
        client.deleteUser(uid).enqueue(BaseRetrofitCallback(deleteUserStatus::setValue))
    }
}