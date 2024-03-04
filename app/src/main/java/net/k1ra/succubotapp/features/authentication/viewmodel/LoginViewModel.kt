package net.k1ra.succubotapp.features.authentication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.di.EncryptedSharedPrefProvider
import net.k1ra.succubotapp.features.authentication.model.LoginRequest
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.authentication.repository.LoginRetrofitInterface
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    retrofit: Retrofit,
    val currentUser: User?,
    private val sharedpref: EncryptedSharedPrefProvider,
    private val gson: Gson
) : ViewModel() {
    private val client = retrofit.create(LoginRetrofitInterface::class.java)

    val loginStatus = MutableLiveData<LoadingStatus<User>>()

    fun login(username: String, password: String) = CoroutineScope(Dispatchers.IO).launch {
        client.login(LoginRequest(username, password)).enqueue(BaseRetrofitCallback(loginStatus::setValue){
            sharedpref.setStored(Constants.CurrentUserName, gson.toJson(it))
        })
    }
}